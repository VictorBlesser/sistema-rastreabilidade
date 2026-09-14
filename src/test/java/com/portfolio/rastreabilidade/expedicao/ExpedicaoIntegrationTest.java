package com.portfolio.rastreabilidade.expedicao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = "ADMINISTRADOR")
class ExpedicaoIntegrationTest {

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
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final List<Long> expedicaoIds = new ArrayList<>();
    private final List<Long> recebimentoIds = new ArrayList<>();

    private Long produtoId;

    @BeforeEach
    void preparar() {
        produtoId = produtoRepository.saveAndFlush(new Produto(
                "EXP-" + UUID.randomUUID(),
                "Produto de teste da expedição",
                null,
                TipoProduto.PRODUTO_MEDICO,
                false,
                false,
                true,
                UnidadeMedida.KG)).getId();
    }

    @AfterEach
    void limpar() {
        new TransactionTemplate(transactionManager).executeWithoutResult(
                status -> {
                    for (Long id : expedicaoIds) {
                        movimentacaoRepository.deleteAll(
                                movimentacoesDaExpedicao(id));
                    }

                    for (Long id : recebimentoIds) {
                        movimentacaoRepository.deleteAll(
                                movimentacaoRepository
                                        .findByRecebimentoItemRecebimentoIdOrderByIdAsc(id));
                    }

                    movimentacaoRepository.flush();

                    for (Long id : expedicaoIds) {
                        expedicaoRepository.deleteById(id);
                    }

                    expedicaoRepository.flush();

                    for (Long id : recebimentoIds) {
                        recebimentoRepository.deleteById(id);
                    }

                    recebimentoRepository.flush();

                    if (produtoId != null) {
                        produtoRepository.deleteById(produtoId);
                        produtoRepository.flush();
                    }
                });
    }

    @Test
    void rascunhoNaoDeveAlterarEstoque() {
        registrarEntrada("10");

        Long id = criarExpedicao("3");

        verificarRascunhoSemSaidas(id);
        verificarSaldo("10");
    }

    @Test
    void deveConfirmarSaidaComAuditoria() {
        registrarEntrada("10");

        Long id = criarExpedicao("2.75");
        expedicaoService.confirmar(id);

        Expedicao expedicao = expedicaoService.buscarPorId(id);
        List<MovimentacaoEstoque> movimentos = movimentacoesDaExpedicao(id);

        assertEquals(StatusExpedicao.CONFIRMADO, expedicao.getStatus());
        assertNotNull(expedicao.getConfirmadoEm());
        assertEquals("admin", expedicao.getConfirmadoPor().getLogin());
        assertEquals(1, movimentos.size());

        MovimentacaoEstoque movimento = movimentos.get(0);

        assertEquals(TipoMovimentacao.SAIDA, movimento.getTipo());
        assertEquals(produtoId, movimento.getProduto().getId());
        assertNull(movimento.getRecebimentoItem());
        assertNull(movimento.getLote());
        assertEquals(
                expedicao.getItens().get(0).getId(),
                movimento.getExpedicaoItem().getId());
        assertEquals("admin", movimento.getRegistradoPor().getLogin());
        assertEquals(
                expedicao.getConfirmadoEm().toInstant(),
                movimento.getRegistradoEm().toInstant());
        assertEquals(
                0,
                new BigDecimal("2.75").compareTo(movimento.getQuantidade()));

        verificarSaldo("7.25");
    }

    @Test
    void devePermitirConsumirSaldoExato() {
        registrarEntrada("5");

        Long id = criarExpedicao("5");
        expedicaoService.confirmar(id);

        verificarSaldo("0");
        assertEquals(1, movimentacoesDaExpedicao(id).size());
    }

    @Test
    void deveRecusarQuantidadeAcimaDoSaldo() {
        registrarEntrada("5");

        Long id = criarExpedicao("6");

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.confirmar(id));

        assertTrue(erro.getMessage().contains("Saldo insuficiente"));
        verificarRascunhoSemSaidas(id);
        verificarSaldo("5");
    }

    @Test
    void deveRecusarSaidaSemEstoque() {
        Long id = criarExpedicao("1");

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.confirmar(id));

        assertTrue(erro.getMessage().contains("Saldo insuficiente"));
        verificarRascunhoSemSaidas(id);
        verificarSaldo("0");
    }

    @Test
    void deveSomarItensRepetidosAntesDeValidarSaldo() {
        registrarEntrada("5");

        Long id = criarExpedicao("3", "3");

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.confirmar(id));

        assertTrue(erro.getMessage().contains("Saldo insuficiente"));
        verificarRascunhoSemSaidas(id);
        verificarSaldo("5");
    }

    @Test
    void deveConfirmarItensRepetidosComSaldoSuficiente() {
        registrarEntrada("5");

        Long id = criarExpedicao("2", "3");
        expedicaoService.confirmar(id);

        assertEquals(2, movimentacoesDaExpedicao(id).size());
        verificarSaldo("0");
    }

    @Test
    void naoDeveConfirmarDuasVezes() {
        registrarEntrada("10");

        Long id = criarExpedicao("3");
        expedicaoService.confirmar(id);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.confirmar(id));

        assertTrue(erro.getMessage().contains("já foi confirmada"));
        assertEquals(1, movimentacoesDaExpedicao(id).size());
        verificarSaldo("7");
    }

    @Test
    void naoDeveAdicionarItemDepoisDaConfirmacao() {
        registrarEntrada("10");

        Long id = criarExpedicao("3");
        expedicaoService.confirmar(id);

        assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.adicionarItem(
                        id,
                        produtoId,
                        null,
                        new BigDecimal("1")));

        assertEquals(1, expedicaoService.buscarPorId(id).getItens().size());
        assertEquals(1, movimentacoesDaExpedicao(id).size());
        verificarSaldo("7");
    }

    @Test
    void naoDeveConfirmarSemItens() {
        Long id = criarExpedicao();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.confirmar(id));

        assertTrue(erro.getMessage().contains("pelo menos um item"));
        verificarRascunhoSemSaidas(id);
    }

    @Test
    @WithAnonymousUser
    void deveExigirUsuarioAutenticadoParaConfirmar() {
        Long id = criarExpedicao("1");

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.confirmar(id));

        assertTrue(erro.getMessage().contains("Entre no sistema"));
        verificarRascunhoSemSaidas(id);
        verificarSaldo("0");
    }

    private void registrarEntrada(String quantidade) {
        Long id = recebimentoService.criar(
                "Fornecedor de teste",
                "REC-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 14));

        recebimentoIds.add(id);

        recebimentoService.adicionarItem(
                id,
                produtoId,
                null,
                new BigDecimal(quantidade));

        recebimentoService.confirmar(id);
    }

    private Long criarExpedicao(String... quantidades) {
        Long id = expedicaoService.criar(
                "Destinatário de teste",
                "EXP-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 14));

        expedicaoIds.add(id);

        for (String quantidade : quantidades) {
            expedicaoService.adicionarItem(
                    id,
                    produtoId,
                    null,
                    new BigDecimal(quantidade));
        }

        return id;
    }

    private List<MovimentacaoEstoque> movimentacoesDaExpedicao(Long id) {
        return movimentacaoRepository
                .findByExpedicaoItemExpedicaoIdOrderByIdAsc(id);
    }

    private void verificarSaldo(String esperado) {
        BigDecimal saldo = movimentacaoRepository.consultarSaldo(
                produtoId,
                null,
                TipoMovimentacao.ENTRADA);

        assertEquals(0, new BigDecimal(esperado).compareTo(saldo));
    }

    private void verificarRascunhoSemSaidas(Long id) {
        Expedicao expedicao = expedicaoService.buscarPorId(id);

        assertEquals(StatusExpedicao.RASCUNHO, expedicao.getStatus());
        assertNull(expedicao.getConfirmadoEm());
        assertNull(expedicao.getConfirmadoPor());
        assertTrue(movimentacoesDaExpedicao(id).isEmpty());
    }
}