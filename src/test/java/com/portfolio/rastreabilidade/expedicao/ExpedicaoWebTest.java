package com.portfolio.rastreabilidade.expedicao;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoqueRepository;
import com.portfolio.rastreabilidade.estoque.TipoMovimentacao;
import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.produto.TipoProduto;
import com.portfolio.rastreabilidade.recebimento.RecebimentoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = "ADMINISTRADOR")
class ExpedicaoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpedicaoService service;

    @Autowired
    private RecebimentoService recebimentoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    private Long expedicaoId;
    private Long produtoId;

    @BeforeEach
    void preparar() {
        produtoId = produtoRepository.saveAndFlush(new Produto(
                "EW-" + UUID.randomUUID(),
                "Produto de teste da tela de expedição",
                TipoProduto.PRODUTO_MEDICO,
                false,
                false)).getId();

        expedicaoId = service.criar(
                "Destinatário web",
                "EXP-WEB",
                LocalDate.of(2026, 9, 14));
    }

    @Test
    void deveRenderizarListaFormularioEDetalhe() throws Exception {
        mockMvc.perform(get("/expedicoes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("EXP-WEB")))
                .andExpect(content().string(
                        containsString("href=\"/expedicoes\"")));

        mockMvc.perform(get("/expedicoes/novo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Criar rascunho")));

        mockMvc.perform(get("/expedicoes/" + expedicaoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Destinatário web")))
                .andExpect(content().string(containsString("Adicionar item")));
    }

    @Test
    void deveCriarRascunhoPeloFormulario() throws Exception {
        String destino = mockMvc.perform(post("/expedicoes")
                        .with(csrf())
                        .param("destinatario", "Destinatário cadastrado pela tela")
                        .param("documento", "EXP-NOVA")
                        .param("dataExpedicao", "2026-09-14"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensagemSucesso"))
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertTrue(destino != null && destino.startsWith("/expedicoes/"));

        Long id = Long.valueOf(
                destino.substring(destino.lastIndexOf('/') + 1));

        Expedicao expedicao = service.buscarPorId(id);

        assertEquals("EXP-NOVA", expedicao.getDocumento());
        assertTrue(expedicao.isRascunho());
    }

    @Test
    void deveRejeitarCabecalhoIncompleto() throws Exception {
        mockMvc.perform(post("/expedicoes")
                        .with(csrf())
                        .param("destinatario", "")
                        .param("documento", "")
                        .param("dataExpedicao", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(
                        "expedicaoForm",
                        "destinatario",
                        "documento",
                        "dataExpedicao"));
    }

    @Test
    void deveAdicionarItemEConfirmarPelaTela() throws Exception {
        registrarEntrada("5");

        mockMvc.perform(post("/expedicoes/" + expedicaoId + "/itens")
                        .with(csrf())
                        .param("produtoId", produtoId.toString())
                        .param("quantidade", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expedicoes/" + expedicaoId));

        mockMvc.perform(get("/expedicoes/" + expedicaoId))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("Confirmar saída do estoque")));

        mockMvc.perform(post("/expedicoes/" + expedicaoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensagemSucesso"));

        assertEquals(
                StatusExpedicao.CONFIRMADO,
                service.buscarPorId(expedicaoId).getStatus());

        assertEquals(1, movimentacaoRepository
                .findByExpedicaoItemExpedicaoIdOrderByIdAsc(expedicaoId)
                .size());

        verificarSaldo("3");

        mockMvc.perform(get("/expedicoes/" + expedicaoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Confirmado por")))
                .andExpect(content().string(not(containsString("Adicionar item"))))
                .andExpect(content().string(
                        not(containsString("Confirmar saída do estoque"))));
    }

    @Test
    void deveRejeitarItemSemProdutoEQuantidade() throws Exception {
        mockMvc.perform(post("/expedicoes/" + expedicaoId + "/itens")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(
                        "itemForm", "produtoId", "quantidade"));

        assertTrue(service.buscarPorId(expedicaoId).getItens().isEmpty());
    }

    @Test
    void deveExibirErroQuandoSaldoForInsuficiente() throws Exception {
        registrarEntrada("1");

        service.adicionarItem(
                expedicaoId, produtoId, null, new BigDecimal("2"));

        mockMvc.perform(post("/expedicoes/" + expedicaoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expedicoes/" + expedicaoId))
                .andExpect(flash().attribute(
                        "mensagemErro", containsString("Saldo insuficiente")));

        assertTrue(service.buscarPorId(expedicaoId).isRascunho());

        assertTrue(movimentacaoRepository
                .findByExpedicaoItemExpedicaoIdOrderByIdAsc(expedicaoId)
                .isEmpty());

        verificarSaldo("1");
    }

    @Test
    void deveRecusarConfirmacaoRepetidaSemDuplicarSaida() throws Exception {
        registrarEntrada("5");

        service.adicionarItem(
                expedicaoId, produtoId, null, new BigDecimal("2"));

        service.confirmar(expedicaoId);

        mockMvc.perform(post("/expedicoes/" + expedicaoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(
                        "mensagemErro", "A expedição já foi confirmada"));

        assertEquals(1, movimentacaoRepository
                .findByExpedicaoItemExpedicaoIdOrderByIdAsc(expedicaoId)
                .size());

        verificarSaldo("3");
    }

    @Test
    void deveRedirecionarQuandoExpedicaoNaoExiste() throws Exception {
        mockMvc.perform(get("/expedicoes/0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/expedicoes"))
                .andExpect(flash().attribute(
                        "mensagemErro", "Expedição não encontrada"));
    }

    @Test
    @WithAnonymousUser
    void deveExigirLogin() throws Exception {
        mockMvc.perform(get("/expedicoes"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/expedicoes/" + expedicaoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertTrue(service.buscarPorId(expedicaoId).isRascunho());
    }

    @Test
    void deveRecusarConfirmacaoSemCsrf() throws Exception {
        mockMvc.perform(post("/expedicoes/" + expedicaoId + "/confirmar"))
                .andExpect(status().isForbidden());

        assertTrue(service.buscarPorId(expedicaoId).isRascunho());
    }

    private void registrarEntrada(String quantidade) {
        Long id = recebimentoService.criar(
                "Fornecedor web",
                "REC-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 14));

        recebimentoService.adicionarItem(
                id, produtoId, null, new BigDecimal(quantidade));

        recebimentoService.confirmar(id);
    }

    private void verificarSaldo(String esperado) {
        BigDecimal saldo = movimentacaoRepository.consultarSaldo(
                produtoId,
                null,
                TipoMovimentacao.ENTRADA);

        assertEquals(0, new BigDecimal(esperado).compareTo(saldo));
    }
}