package com.portfolio.rastreabilidade.expedicao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoqueRepository;
import com.portfolio.rastreabilidade.estoque.TipoMovimentacao;
import com.portfolio.rastreabilidade.lote.LoteService;
import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.produto.TipoProduto;
import com.portfolio.rastreabilidade.produto.UnidadeMedida;
import com.portfolio.rastreabilidade.recebimento.RecebimentoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = "ADMINISTRADOR")
class ExpedicaoLoteIntegrationTest {

    private static final LocalDate DATA = LocalDate.of(2026, 9, 14);

    @Autowired
    private ExpedicaoService expedicaoService;

    @Autowired
    private RecebimentoService recebimentoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private LoteService loteService;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    private Long produtoId;
    private Long loteId;
    private Long outroLoteId;

    @BeforeEach
    void preparar() {
        produtoId = criarProduto();

        loteId = loteService.cadastrar(
                produtoId,
                "LOTE-A",
                null,
                DATA.plusMonths(6)).getId();

        outroLoteId = loteService.cadastrar(
                produtoId,
                "LOTE-B",
                null,
                DATA.plusMonths(12)).getId();
    }

    @Test
    void deveBaixarSomenteOLoteSelecionado() {
        registrarEntrada(loteId, "5");
        registrarEntrada(outroLoteId, "8");

        Long id = criarExpedicao();

        expedicaoService.adicionarItem(
                id, produtoId, loteId, new BigDecimal("2.5"));

        expedicaoService.confirmar(id);

        verificarSaldo(loteId, "2.5");
        verificarSaldo(outroLoteId, "8");

        var movimentos = movimentacaoRepository
                .findByExpedicaoItemExpedicaoIdOrderByIdAsc(id);

        assertEquals(1, movimentos.size());
        assertEquals(loteId, movimentos.get(0).getLote().getId());
        assertEquals(TipoMovimentacao.SAIDA, movimentos.get(0).getTipo());
    }

    @Test
    void naoDeveUsarSaldoDeOutroLote() {
        registrarEntrada(loteId, "2");
        registrarEntrada(outroLoteId, "10");

        Long id = criarExpedicao();

        expedicaoService.adicionarItem(
                id, produtoId, loteId, new BigDecimal("3"));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.confirmar(id));

        assertTrue(erro.getMessage().contains("Saldo insuficiente"));
    }

    @Test
    void deveExigirLoteParaProdutoControlado() {
        Long id = criarExpedicao();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.adicionarItem(
                        id, produtoId, null, BigDecimal.ONE));

        assertTrue(erro.getMessage().contains("O lote é obrigatório"));
    }

    @Test
    void deveRecusarLoteDeOutroProduto() {
        Long outroProdutoId = criarProduto();

        Long loteDeOutroProduto = loteService.cadastrar(
                outroProdutoId,
                "LOTE-OUTRO",
                null,
                DATA.plusMonths(6)).getId();

        Long id = criarExpedicao();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.adicionarItem(
                        id, produtoId, loteDeOutroProduto, BigDecimal.ONE));

        assertTrue(erro.getMessage().contains(
                "O lote não pertence ao produto informado"));
    }

    @Test
    void deveRecusarLoteVencidoNaDataDaExpedicao() {
        Long loteVencidoId = loteService.cadastrar(
                produtoId,
                "LOTE-VENCIDO",
                null,
                DATA.minusDays(1)).getId();

        Long id = criarExpedicao();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> expedicaoService.adicionarItem(
                        id, produtoId, loteVencidoId, BigDecimal.ONE));

        assertTrue(erro.getMessage().contains(
                "O lote está vencido na data da expedição"));
    }

    @Test
    void devePermitirSaidaNoDiaDaValidade() {
        Long loteValidoHojeId = loteService.cadastrar(
                produtoId,
                "LOTE-VALIDADE-HOJE",
                null,
                DATA).getId();

        registrarEntrada(loteValidoHojeId, "2");

        Long id = criarExpedicao();

        expedicaoService.adicionarItem(
                id, produtoId, loteValidoHojeId, BigDecimal.ONE);

        expedicaoService.confirmar(id);

        verificarSaldo(loteValidoHojeId, "1");

        assertEquals(
                StatusExpedicao.CONFIRMADO,
                expedicaoService.buscarPorId(id).getStatus());
    }

    private Long criarProduto() {
        return produtoRepository.saveAndFlush(new Produto(
                "EXL-" + UUID.randomUUID(),
                "Produto de teste com lote",
                null,
                TipoProduto.PRODUTO_MEDICO,
                true,
                true,
                true,
                UnidadeMedida.KG)).getId();
    }

    private Long criarExpedicao() {
        return expedicaoService.criar(
                "Destinatário de teste",
                "EXP-" + UUID.randomUUID(),
                DATA);
    }

    private void registrarEntrada(Long lote, String quantidade) {
        Long id = recebimentoService.criar(
                "Fornecedor de teste",
                "REC-" + UUID.randomUUID(),
                DATA);

        recebimentoService.adicionarItem(
                id, produtoId, lote, new BigDecimal(quantidade));

        recebimentoService.confirmar(id);
    }

    private void verificarSaldo(Long lote, String esperado) {
        BigDecimal saldo = movimentacaoRepository.consultarSaldo(
                produtoId,
                lote,
                TipoMovimentacao.ENTRADA);

        assertEquals(0, new BigDecimal(esperado).compareTo(saldo));
    }
}