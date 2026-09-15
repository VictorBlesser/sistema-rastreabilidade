package com.portfolio.rastreabilidade.recebimento;

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
import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.produto.TipoProduto;

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
class RecebimentoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecebimentoService service;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    private Long recebimentoId;
    private Long produtoId;

    @BeforeEach
    void preparar() {
        produtoId = produtoRepository.saveAndFlush(new Produto(
                "WEB-" + UUID.randomUUID(),
                "Produto de teste web",
                TipoProduto.PRODUTO_MEDICO,
                false,
                false)).getId();

        recebimentoId = service.criar(
                "Fornecedor web",
                "DOC-WEB",
                LocalDate.of(2026, 9, 14));
    }

    @Test
    void deveRenderizarListaFormularioEDetalhe() throws Exception {
        mockMvc.perform(get("/recebimentos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DOC-WEB")));

        mockMvc.perform(get("/recebimentos/novo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Criar rascunho")));

        mockMvc.perform(get("/recebimentos/" + recebimentoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fornecedor web")))
                .andExpect(content().string(containsString("Adicionar item")));
    }

    @Test
    @WithMockUser(authorities = "RECEBIMENTO_CRIAR")
    void deveCriarRascunhoPeloFormulario() throws Exception {
        mockMvc.perform(get("/recebimentos/novo"))
                .andExpect(status().isOk());

        String destino = mockMvc.perform(post("/recebimentos")
                        .with(csrf())
                        .param("fornecedor", "Fornecedor cadastrado pela tela")
                        .param("documento", "DOC-NOVO")
                        .param("dataRecebimento", "2026-09-14"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensagemSucesso"))
                .andReturn()
                .getResponse()
                .getRedirectedUrl();

        assertTrue(destino != null && destino.startsWith("/recebimentos/"));

        Long id = Long.valueOf(
                destino.substring(destino.lastIndexOf('/') + 1));

        Recebimento recebimento = service.buscarPorId(id);

        assertEquals("DOC-NOVO", recebimento.getDocumento());
        assertTrue(recebimento.isRascunho());
    }

    @Test
    void deveRejeitarCabecalhoIncompleto() throws Exception {
        mockMvc.perform(post("/recebimentos")
                        .with(csrf())
                        .param("fornecedor", "")
                        .param("documento", "")
                        .param("dataRecebimento", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(
                        "recebimentoForm",
                        "fornecedor",
                        "documento",
                        "dataRecebimento"));
    }

    @Test
    void deveAdicionarItemEConfirmarPelaTela() throws Exception {
        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/itens")
                        .with(csrf())
                        .param("produtoId", produtoId.toString())
                        .param("quantidade", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recebimentos/" + recebimentoId));

        mockMvc.perform(get("/recebimentos/" + recebimentoId))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("Produto de teste web")))
                .andExpect(content().string(
                        containsString("Confirmar entrada no estoque")));

        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensagemSucesso"));

        assertEquals(
                StatusRecebimento.CONFIRMADO,
                service.buscarPorId(recebimentoId).getStatus());

        assertEquals(1, movimentacaoRepository
                .findByRecebimentoItemRecebimentoIdOrderByIdAsc(recebimentoId)
                .size());

        mockMvc.perform(get("/recebimentos/" + recebimentoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Confirmado por")))
                .andExpect(content().string(not(containsString("Adicionar item"))))
                .andExpect(content().string(
                        not(containsString("Confirmar entrada no estoque"))));
    }

    @Test
    void deveRejeitarItemSemProdutoEQuantidade() throws Exception {
        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/itens")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(
                        "itemForm", "produtoId", "quantidade"));

        assertTrue(service.buscarPorId(recebimentoId).getItens().isEmpty());
    }

    @Test
    void deveExibirErroParaQuantidadeFracionadaEmProdutoInteiro()
            throws Exception {

        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/itens")
                        .with(csrf())
                        .param("produtoId", produtoId.toString())
                        .param("quantidade", "1.5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "Este produto aceita somente quantidades inteiras")));

        assertTrue(service.buscarPorId(recebimentoId).getItens().isEmpty());
    }

    @Test
    void deveRecusarConfirmacaoRepetidaSemDuplicarEstoque() throws Exception {
        service.adicionarItem(
                recebimentoId, produtoId, null, new BigDecimal("2"));

        service.confirmar(recebimentoId);

        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(
                        "mensagemErro", "O recebimento já foi confirmado"));

        assertEquals(1, movimentacaoRepository
                .findByRecebimentoItemRecebimentoIdOrderByIdAsc(recebimentoId)
                .size());
    }

    @Test
    void deveRedirecionarQuandoRecebimentoNaoExiste() throws Exception {
        mockMvc.perform(get("/recebimentos/0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/recebimentos"))
                .andExpect(flash().attribute(
                        "mensagemErro", "Recebimento não encontrado"));
    }

    @Test
    @WithAnonymousUser
    void deveExigirLogin() throws Exception {
        mockMvc.perform(get("/recebimentos"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertTrue(service.buscarPorId(recebimentoId).isRascunho());
    }

    @Test
    void deveRecusarConfirmacaoSemCsrf() throws Exception {
        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/confirmar"))
                .andExpect(status().isForbidden());

        assertTrue(service.buscarPorId(recebimentoId).isRascunho());
    }

    @Test
    @WithMockUser(authorities = "RECEBIMENTO_VISUALIZAR")
    void consultaNaoDevePermitirAlteracoes() throws Exception {
        service.adicionarItem(
                recebimentoId, produtoId, null, new BigDecimal("2"));

        mockMvc.perform(get("/recebimentos"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/recebimentos/" + recebimentoId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/recebimentos/novo"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/recebimentos")
                        .with(csrf())
                        .param("fornecedor", "Fornecedor")
                        .param("documento", "NEGADO")
                        .param("dataRecebimento", "2026-09-14"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/itens")
                        .with(csrf())
                        .param("produtoId", produtoId.toString())
                        .param("quantidade", "1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(1, service.buscarPorId(recebimentoId).getItens().size());
        verificarRascunhoSemMovimentacoes();
    }

        @Test
    @WithMockUser(authorities = "RECEBIMENTO_EDITAR")
    void editarItensNaoDevePermitirConfirmacao() throws Exception {
        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/itens")
                        .with(csrf())
                        .param("produtoId", produtoId.toString())
                        .param("quantidade", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensagemSucesso"));

        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertEquals(1, service.buscarPorId(recebimentoId).getItens().size());
        verificarRascunhoSemMovimentacoes();
    }

    @Test
    @WithMockUser(
            username = "admin",
            authorities = "RECEBIMENTO_CONFIRMAR")
    void deveConfirmarComPermissaoEspecifica() throws Exception {
        service.adicionarItem(
                recebimentoId, produtoId, null, new BigDecimal("2"));

        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensagemSucesso"));

        assertEquals(
                StatusRecebimento.CONFIRMADO,
                service.buscarPorId(recebimentoId).getStatus());

        assertEquals(1, movimentacaoRepository
                .findByRecebimentoItemRecebimentoIdOrderByIdAsc(recebimentoId)
                .size());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void nomeDoPerfilNaoDeveSubstituirPermissao() throws Exception {
        mockMvc.perform(get("/recebimentos"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/recebimentos/" + recebimentoId + "/confirmar")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verificarRascunhoSemMovimentacoes();
    }

    private void verificarRascunhoSemMovimentacoes() {
        assertTrue(service.buscarPorId(recebimentoId).isRascunho());

        assertTrue(movimentacaoRepository
                .findByRecebimentoItemRecebimentoIdOrderByIdAsc(recebimentoId)
                .isEmpty());
    }
}