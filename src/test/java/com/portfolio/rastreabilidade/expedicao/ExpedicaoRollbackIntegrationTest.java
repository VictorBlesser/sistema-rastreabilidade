package com.portfolio.rastreabilidade.expedicao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoque;
import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoqueRepository;
import com.portfolio.rastreabilidade.estoque.TipoMovimentacao;
import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.produto.TipoProduto;
import com.portfolio.rastreabilidade.produto.UnidadeMedida;
import com.portfolio.rastreabilidade.recebimento.RecebimentoRepository;
import com.portfolio.rastreabilidade.recebimento.RecebimentoService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = "ADMINISTRADOR")
class ExpedicaoRollbackIntegrationTest {

    @Autowired
    private ExpedicaoService expedicaoService;

    @Autowired
    private ExpedicaoRepository expedicaoRepository;

    @Autowired
    private RecebimentoService recebimentoService;

    @Autowired
    private RecebimentoRepository recebimentoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoSpyBean
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    private Long produtoId;
    private Long recebimentoId;
    private Long expedicaoId;

    @BeforeEach
    void preparar() {
        produtoId = produtoRepository.saveAndFlush(new Produto(
                "RB-" + UUID.randomUUID(),
                "Produto de teste de rollback",
                null,
                TipoProduto.PRODUTO_MEDICO,
                false,
                false,
                true,
                UnidadeMedida.KG)).getId();

        recebimentoId = recebimentoService.criar(
                "Fornecedor de teste",
                "REC-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 14));

        recebimentoService.adicionarItem(
                recebimentoId,
                produtoId,
                null,
                new BigDecimal("10"));

        recebimentoService.confirmar(recebimentoId);

        expedicaoId = expedicaoService.criar(
                "Destinatário de teste",
                "EXP-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 14));

        expedicaoService.adicionarItem(
                expedicaoId,
                produtoId,
                null,
                new BigDecimal("2"));

        expedicaoService.adicionarItem(
                expedicaoId,
                produtoId,
                null,
                new BigDecimal("3"));
    }

    @AfterEach
    void limpar() {
        reset(movimentacaoRepository);

        new TransactionTemplate(transactionManager).executeWithoutResult(
                status -> {
                    if (expedicaoId != null) {
                        movimentacaoRepository.deleteAll(
                                movimentacaoRepository
                                        .findByExpedicaoItemExpedicaoIdOrderByIdAsc(
                                                expedicaoId));
                    }

                    if (recebimentoId != null) {
                        movimentacaoRepository.deleteAll(
                                movimentacaoRepository
                                        .findByRecebimentoItemRecebimentoIdOrderByIdAsc(
                                                recebimentoId));
                    }

                    movimentacaoRepository.flush();

                    if (expedicaoId != null) {
                        expedicaoRepository.deleteById(expedicaoId);
                        expedicaoRepository.flush();
                    }

                    if (recebimentoId != null) {
                        recebimentoRepository.deleteById(recebimentoId);
                        recebimentoRepository.flush();
                    }

                    if (produtoId != null) {
                        produtoRepository.deleteById(produtoId);
                        produtoRepository.flush();
                    }
                });
    }

    @Test
    void deveDesfazerSaidaParcialEPermitirNovaConfirmacao() {
        doAnswer(invocacao -> {
            List<MovimentacaoEstoque> movimentos = invocacao.getArgument(0);

            movimentacaoRepository.saveAndFlush(movimentos.get(0));

            throw new IllegalStateException(
                    "Falha simulada após a primeira saída");
        }).when(movimentacaoRepository).saveAllAndFlush(anyList());

        IllegalStateException erro = assertThrows(
                IllegalStateException.class,
                () -> expedicaoService.confirmar(expedicaoId));

        assertEquals(
                "Falha simulada após a primeira saída",
                erro.getMessage());

        Expedicao expedicao = expedicaoService.buscarPorId(expedicaoId);

        assertEquals(StatusExpedicao.RASCUNHO, expedicao.getStatus());
        assertNull(expedicao.getConfirmadoEm());
        assertNull(expedicao.getConfirmadoPor());
        assertEquals(2, expedicao.getItens().size());

        assertTrue(movimentacaoRepository
                .findByExpedicaoItemExpedicaoIdOrderByIdAsc(expedicaoId)
                .isEmpty());

        verificarSaldo("10");

        reset(movimentacaoRepository);

        expedicaoService.confirmar(expedicaoId);

        assertEquals(
                StatusExpedicao.CONFIRMADO,
                expedicaoService.buscarPorId(expedicaoId).getStatus());

        assertEquals(
                2,
                movimentacaoRepository
                        .findByExpedicaoItemExpedicaoIdOrderByIdAsc(expedicaoId)
                        .size());

        verificarSaldo("5");
    }

    private void verificarSaldo(String esperado) {
        BigDecimal saldo = movimentacaoRepository.consultarSaldo(
                produtoId,
                null,
                TipoMovimentacao.ENTRADA);

        assertEquals(0, new BigDecimal(esperado).compareTo(saldo));
    }
}