package com.portfolio.rastreabilidade.estoque;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithUserDetails("admin")
class EstoqueIntegrationTest {

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private RecebimentoService recebimentoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private LoteService loteService;

    @Autowired
    private MockMvc mockMvc;

    private Long produtoId;
    private Long loteId;
    private Long outroLoteId;

    @BeforeEach
    void preparar() {
        produtoId = produtoRepository.saveAndFlush(new Produto(
                "EST-" + UUID.randomUUID(),
                "Produto de teste do estoque",
                null,
                TipoProduto.PRODUTO_MEDICO,
                true,
                true,
                true,
                UnidadeMedida.KG)).getId();

        loteId = loteService.cadastrar(
                produtoId,
                "EST-001",
                null,
                LocalDate.of(2028, 1, 1)).getId();

        outroLoteId = loteService.cadastrar(
                produtoId,
                "EST-002",
                null,
                LocalDate.of(2028, 2, 1)).getId();
    }

    @Test
    void naoDeveContarRascunhoNoEstoque() {
        criarRecebimento(loteId, "2.75");

        assertTrue(saldosDoProduto().isEmpty());
    }

    @Test
    void deveSomarEntradasDoMesmoProdutoELote() {
        recebimentoService.confirmar(criarRecebimento(loteId, "2.75"));
        recebimentoService.confirmar(criarRecebimento(loteId, "1.25"));

        List<SaldoEstoque> saldos = saldosDoProduto();

        assertEquals(1, saldos.size());
        assertEquals(loteId, saldos.get(0).getLoteId());
        assertEquals(
                0,
                new BigDecimal("4").compareTo(saldos.get(0).getQuantidade()));
        assertEquals(UnidadeMedida.KG, saldos.get(0).getUnidadeMedida());
    }

    @Test
    void deveSepararSaldosDeLotesDiferentes() {
        recebimentoService.confirmar(criarRecebimento(loteId, "2"));
        recebimentoService.confirmar(criarRecebimento(outroLoteId, "3"));

        List<SaldoEstoque> saldos = saldosDoProduto();

        assertEquals(2, saldos.size());

        SaldoEstoque primeiro = saldos.stream()
                .filter(saldo -> loteId.equals(saldo.getLoteId()))
                .findFirst()
                .orElseThrow();

        SaldoEstoque segundo = saldos.stream()
                .filter(saldo -> outroLoteId.equals(saldo.getLoteId()))
                .findFirst()
                .orElseThrow();

        assertEquals(0, new BigDecimal("2").compareTo(primeiro.getQuantidade()));
        assertEquals(0, new BigDecimal("3").compareTo(segundo.getQuantidade()));
    }

    @Test
    void deveConsultarProdutoSemLote() {
        Long semLoteId = produtoRepository.saveAndFlush(new Produto(
                "SEM-" + UUID.randomUUID(),
                "Produto sem lote",
                TipoProduto.PRODUTO_MEDICO,
                false,
                false)).getId();

        Long recebimentoId = recebimentoService.criar(
                "Fornecedor", "SEM-LOTE", LocalDate.of(2026, 9, 14));

        recebimentoService.adicionarItem(
                recebimentoId, semLoteId, null, new BigDecimal("5"));

        recebimentoService.confirmar(recebimentoId);

        SaldoEstoque saldo = estoqueService.listarSaldos().stream()
                .filter(item -> semLoteId.equals(item.getProdutoId()))
                .findFirst()
                .orElseThrow();

        assertTrue(saldo.getLoteId() == null);
        assertEquals(0, new BigDecimal("5").compareTo(saldo.getQuantidade()));
    }

    @Test
    void deveRenderizarTelaDeEstoque() throws Exception {
        recebimentoService.confirmar(criarRecebimento(loteId, "2.75"));

        mockMvc.perform(get("/estoque"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("Produto de teste do estoque")))
                .andExpect(content().string(containsString("EST-001")))
                .andExpect(content().string(containsString("2,750000")));
    }

    @Test
    @WithAnonymousUser
    void deveExigirLogin() throws Exception {
        mockMvc.perform(get("/estoque"))
                .andExpect(status().is3xxRedirection());
    }

    private Long criarRecebimento(Long lote, String quantidade) {
        Long id = recebimentoService.criar(
                "Fornecedor",
                "DOC-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 14));

        recebimentoService.adicionarItem(
                id, produtoId, lote, new BigDecimal(quantidade));

        return id;
    }

    private List<SaldoEstoque> saldosDoProduto() {
        return estoqueService.listarSaldos().stream()
                .filter(saldo -> produtoId.equals(saldo.getProdutoId()))
                .toList();
    }
}