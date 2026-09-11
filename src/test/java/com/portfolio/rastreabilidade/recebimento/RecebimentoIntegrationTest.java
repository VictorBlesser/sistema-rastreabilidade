package com.portfolio.rastreabilidade.recebimento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = "ADMINISTRADOR")
class RecebimentoIntegrationTest {

    @Autowired
    private RecebimentoService service;

    @Autowired
    private RecebimentoRepository repository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoSpyBean
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    private TransactionTemplate transaction;
    private Long recebimentoId;
    private Long produtoFracionavelId;
    private Long produtoInteiroId;

    @BeforeEach
    void preparar() {
        transaction = new TransactionTemplate(transactionManager);

        String identificador = UUID.randomUUID().toString();

        produtoFracionavelId = produtoRepository.saveAndFlush(new Produto(
                "F-" + identificador,
                "Produto fracionável de teste",
                null,
                TipoProduto.PRODUTO_MEDICO,
                false,
                false,
                true,
                UnidadeMedida.KG)).getId();

        produtoInteiroId = produtoRepository.saveAndFlush(new Produto(
                "I-" + identificador,
                "Produto inteiro de teste",
                null,
                TipoProduto.PRODUTO_MEDICO,
                false,
                false,
                false,
                UnidadeMedida.UN)).getId();

        recebimentoId = service.criar(
                "Fornecedor de teste",
                "DOC-" + identificador,
                LocalDate.of(2026, 9, 11));
    }

    @AfterEach
    void limpar() {
        reset(movimentacaoRepository);

        transaction.executeWithoutResult(status -> {
            movimentacaoRepository.deleteAll(
                    movimentacoes());
            movimentacaoRepository.flush();

            repository.deleteById(recebimentoId);
            repository.flush();

            produtoRepository.deleteById(produtoFracionavelId);
            produtoRepository.deleteById(produtoInteiroId);
            produtoRepository.flush();
        });
    }

    @Test
    void deveCriarRascunhoSemMovimentarEstoque() {
        Recebimento recebimento = service.buscarPorId(recebimentoId);

        assertEquals(StatusRecebimento.RASCUNHO, recebimento.getStatus());
        assertTrue(recebimento.getItens().isEmpty());
        assertNull(recebimento.getConfirmadoEm());
        assertNull(recebimento.getConfirmadoPor());
        assertTrue(movimentacoes().isEmpty());
    }

    @Test
    void deveAdicionarItemSemMovimentarEstoque() {
        adicionarQuantidade("2.75");

        Recebimento recebimento = service.buscarPorId(recebimentoId);

        assertEquals(1, recebimento.getItens().size());
        assertEquals(
                0,
                new BigDecimal("2.75").compareTo(
                        recebimento.getItens().get(0).getQuantidade()));
        assertTrue(recebimento.isRascunho());
        assertTrue(movimentacoes().isEmpty());
    }

    @Test
    void deveConfirmarTodosOsItensComAuditoria() {
        adicionarQuantidade("2.75");

        service.adicionarItem(
                recebimentoId,
                produtoInteiroId,
                null,
                new BigDecimal("3"));

        service.confirmar(recebimentoId);

        Recebimento recebimento = service.buscarPorId(recebimentoId);
        List<MovimentacaoEstoque> entradas = movimentacoes();

        assertEquals(StatusRecebimento.CONFIRMADO, recebimento.getStatus());
        assertNotNull(recebimento.getConfirmadoEm());
        assertEquals("admin", recebimento.getConfirmadoPor().getLogin());
        assertEquals(2, entradas.size());

        MovimentacaoEstoque entradaFracionavel = entradas.stream()
                .filter(entrada -> entrada.getProduto().getId()
                        .equals(produtoFracionavelId))
                .findFirst()
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("2.75").compareTo(
                        entradaFracionavel.getQuantidade()));

        for (MovimentacaoEstoque entrada : entradas) {
            assertEquals(TipoMovimentacao.ENTRADA, entrada.getTipo());
            assertEquals("admin", entrada.getRegistradoPor().getLogin());
            assertNotNull(entrada.getRegistradoEm());
        }
    }

    @Test
    void deveImpedirSegundaConfirmacao() {
        adicionarQuantidade("1");
        service.confirmar(recebimentoId);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmar(recebimentoId));

        assertEquals(1, movimentacoes().size());
    }

    @Test
    void deveImpedirAdicionarItemDepoisDaConfirmacao() {
        adicionarQuantidade("1");
        service.confirmar(recebimentoId);

        assertThrows(
                IllegalArgumentException.class,
                () -> adicionarQuantidade("2"));

        assertEquals(
                1,
                service.buscarPorId(recebimentoId).getItens().size());
        assertEquals(1, movimentacoes().size());
    }

    @Test
    void deveRejeitarConfirmacaoSemItens() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmar(recebimentoId));

        assertTrue(service.buscarPorId(recebimentoId).isRascunho());
        assertTrue(movimentacoes().isEmpty());
    }

    @Test
    void deveRejeitarFracaoParaProdutoInteiro() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.adicionarItem(
                        recebimentoId,
                        produtoInteiroId,
                        null,
                        new BigDecimal("1.5")));

        assertTrue(
                service.buscarPorId(recebimentoId).getItens().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0",
            "-1",
            "0.0000001",
            "10000000000000"
    })
    void deveRejeitarQuantidadeInvalida(String quantidade) {
        assertThrows(
                IllegalArgumentException.class,
                () -> adicionarQuantidade(quantidade));

        assertTrue(
                service.buscarPorId(recebimentoId).getItens().isEmpty());
        assertTrue(movimentacoes().isEmpty());
    }

    @Test
    void deveRejeitarConfirmacaoSemLogin() {
        adicionarQuantidade("1");
        SecurityContextHolder.clearContext();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmar(recebimentoId));

        assertTrue(service.buscarPorId(recebimentoId).isRascunho());
        assertTrue(movimentacoes().isEmpty());
    }

    @Test
    void deveRevalidarProdutoNaConfirmacao() {
        adicionarQuantidade("1");

        transaction.executeWithoutResult(status -> {
            Produto produto = produtoRepository
                    .findById(produtoFracionavelId)
                    .orElseThrow();

            produto.inativar();
        });

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmar(recebimentoId));

        assertTrue(service.buscarPorId(recebimentoId).isRascunho());
        assertTrue(movimentacoes().isEmpty());
    }

    @Test
    void deveDesfazerConfirmacaoSeGravacaoDoEstoqueFalhar() {
        adicionarQuantidade("2.75");

        doThrow(new IllegalStateException("Falha simulada de gravação"))
                .when(movimentacaoRepository)
                .saveAllAndFlush(anyList());

        assertThrows(
                IllegalStateException.class,
                () -> service.confirmar(recebimentoId));

        Recebimento recebimento = service.buscarPorId(recebimentoId);

        assertTrue(recebimento.isRascunho());
        assertNull(recebimento.getConfirmadoEm());
        assertNull(recebimento.getConfirmadoPor());
        assertFalse(recebimento.getItens().isEmpty());
        assertTrue(movimentacoes().isEmpty());
    }

    private void adicionarQuantidade(String quantidade) {
        service.adicionarItem(
                recebimentoId,
                produtoFracionavelId,
                null,
                new BigDecimal(quantidade));
    }

    private List<MovimentacaoEstoque> movimentacoes() {
        return movimentacaoRepository
                .findByRecebimentoItemRecebimentoIdOrderByIdAsc(recebimentoId);
    }
}