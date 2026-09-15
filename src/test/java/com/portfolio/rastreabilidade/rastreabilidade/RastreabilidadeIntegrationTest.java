package com.portfolio.rastreabilidade.rastreabilidade;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.portfolio.rastreabilidade.estoque.TipoMovimentacao;
import com.portfolio.rastreabilidade.expedicao.ExpedicaoService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithUserDetails("admin")
class RastreabilidadeIntegrationTest {

    private static final LocalDate DATA = LocalDate.of(2026, 9, 14);

    @Autowired
    private RastreabilidadeService service;

    @Autowired
    private RecebimentoService recebimentoService;

    @Autowired
    private ExpedicaoService expedicaoService;

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
                "RAS-" + UUID.randomUUID(),
                "Produto de teste de rastreabilidade",
                null,
                TipoProduto.PRODUTO_MEDICO,
                true,
                true,
                true,
                UnidadeMedida.KG)).getId();

        loteId = loteService.cadastrar(
                produtoId, "RAST-A", null, DATA.plusYears(1)).getId();

        outroLoteId = loteService.cadastrar(
                produtoId, "RAST-B", null, DATA.plusYears(1)).getId();
    }

    @Test
    void deveConsultarLoteSemMovimentacoes() {
        var resumo = service.consultar(loteId);

        assertEquals(loteId, resumo.lote().getId());
        assertTrue(resumo.movimentos().isEmpty());
        verificarTotais(resumo, "0", "0", "0");
    }

    @Test
    void naoDeveIncluirRascunhosNoHistorico() {
        criarRecebimento(loteId, "10");
        criarExpedicao(loteId, "2");

        var resumo = service.consultar(loteId);

        assertTrue(resumo.movimentos().isEmpty());
        verificarTotais(resumo, "0", "0", "0");
    }

    @Test
    void deveMostrarOrigemDestinoResponsavelETotais() {
        Long recebimentoId = criarRecebimento(loteId, "10.50");
        recebimentoService.confirmar(recebimentoId);

        Long expedicaoId = criarExpedicao(loteId, "2.25");
        expedicaoService.confirmar(expedicaoId);

        var resumo = service.consultar(loteId);

        assertEquals(2, resumo.movimentos().size());
        verificarTotais(resumo, "10.50", "2.25", "8.25");

        var entrada = resumo.movimentos().get(0);
        var saida = resumo.movimentos().get(1);

        assertEquals(TipoMovimentacao.ENTRADA, entrada.getTipo());
        assertEquals("Fornecedor origem", entrada.getParticipante());
        assertEquals("REC-RAST", entrada.getDocumento());
        assertEquals(recebimentoId, entrada.getRecebimentoId());
        assertNull(entrada.getExpedicaoId());
        assertNotNull(entrada.getRegistradoEm());
        assertNotNull(entrada.getResponsavel());
        assertTrue(!entrada.getResponsavel().isBlank());

        assertEquals(TipoMovimentacao.SAIDA, saida.getTipo());
        assertEquals("Cliente destino", saida.getParticipante());
        assertEquals("EXP-RAST", saida.getDocumento());
        assertEquals(expedicaoId, saida.getExpedicaoId());
        assertNull(saida.getRecebimentoId());
        assertEquals(entrada.getResponsavel(), saida.getResponsavel());

        assertTrue(!saida.getRegistradoEm().isBefore(
                entrada.getRegistradoEm()));
    }

    @Test
    void naoDeveMisturarMovimentacoesDeLotesDiferentes() {
        recebimentoService.confirmar(criarRecebimento(loteId, "3"));
        recebimentoService.confirmar(criarRecebimento(outroLoteId, "7"));

        var primeiro = service.consultar(loteId);
        var segundo = service.consultar(outroLoteId);

        assertEquals(1, primeiro.movimentos().size());
        assertEquals(1, segundo.movimentos().size());

        verificarTotais(primeiro, "3", "0", "3");
        verificarTotais(segundo, "7", "0", "7");
    }

    @Test
    void deveRenderizarConsultaEHistoricoComLinks() throws Exception {
        Long recebimentoId = criarRecebimento(loteId, "10.50");
        recebimentoService.confirmar(recebimentoId);

        Long expedicaoId = criarExpedicao(loteId, "2.25");
        expedicaoService.confirmar(expedicaoId);

        mockMvc.perform(get("/rastreabilidade"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("Consultar histórico")))
                .andExpect(content().string(
                        containsString("href=\"/rastreabilidade\"")));

        mockMvc.perform(get("/rastreabilidade")
                        .param("loteId", loteId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fornecedor origem")))
                .andExpect(content().string(containsString("Cliente destino")))
                .andExpect(content().string(containsString("8,250000")))
                .andExpect(content().string(
                        containsString("href=\"/recebimentos/" + recebimentoId + "\"")))
                .andExpect(content().string(
                        containsString("href=\"/expedicoes/" + expedicaoId + "\"")));
    }

    @Test
    void deveInformarLoteInexistente() throws Exception {
        mockMvc.perform(get("/rastreabilidade").param("loteId", "0"))
                .andExpect(status().isOk())
                .andExpect(model().attribute(
                        "mensagemErro", "Lote não encontrado"))
                .andExpect(content().string(
                        containsString("Lote não encontrado")));
    }

    @Test
    @WithAnonymousUser
    void deveExigirLogin() throws Exception {
        mockMvc.perform(get("/rastreabilidade"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/rastreabilidade")
                        .param("loteId", loteId.toString()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void nomeDoPerfilNaoDeveSubstituirPermissao() throws Exception {
        mockMvc.perform(get("/rastreabilidade"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/rastreabilidade")
                        .param("loteId", loteId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR")
    void acessoAoEstoqueNaoDeveLiberarRastreabilidade() throws Exception {
        mockMvc.perform(get("/rastreabilidade")
                        .param("loteId", loteId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "RASTREABILIDADE_VISUALIZAR")
    void devePermitirConsultaComPermissaoEspecifica() throws Exception {
        mockMvc.perform(get("/rastreabilidade")
                        .param("loteId", loteId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("RAST-A")));
    }

    private Long criarRecebimento(Long lote, String quantidade) {
        Long id = recebimentoService.criar(
                "Fornecedor origem", "REC-RAST", DATA);

        recebimentoService.adicionarItem(
                id, produtoId, lote, new BigDecimal(quantidade));

        return id;
    }

    private Long criarExpedicao(Long lote, String quantidade) {
        Long id = expedicaoService.criar(
                "Cliente destino", "EXP-RAST", DATA);

        expedicaoService.adicionarItem(
                id, produtoId, lote, new BigDecimal(quantidade));

        return id;
    }

    private void verificarTotais(
            RastreabilidadeService.Resumo resumo,
            String entradas,
            String saidas,
            String saldo) {

        assertEquals(
                0, new BigDecimal(entradas).compareTo(resumo.entradas()));
        assertEquals(
                0, new BigDecimal(saidas).compareTo(resumo.saidas()));
        assertEquals(
                0, new BigDecimal(saldo).compareTo(resumo.saldo()));
    }
}