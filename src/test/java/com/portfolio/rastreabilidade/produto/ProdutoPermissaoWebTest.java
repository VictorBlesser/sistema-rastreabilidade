package com.portfolio.rastreabilidade.produto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProdutoPermissaoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository repository;

    private Long produtoId;

    @BeforeEach
    void preparar() {
        produtoId = repository.saveAndFlush(new Produto(
                "PER-" + UUID.randomUUID(),
                "Produto para teste de permissão",
                TipoProduto.PRODUTO_MEDICO,
                false,
                false)).getId();
    }

    @Test
    @WithMockUser(authorities = "PRODUTO_VISUALIZAR")
    void devePermitirConsultaDeListaEDetalhes() throws Exception {
        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/produtos/" + produtoId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PRODUTO_VISUALIZAR")
    void consultaNaoDevePermitirCadastro() throws Exception {
        String codigo = "NEG-" + UUID.randomUUID();

        mockMvc.perform(get("/produtos/novo"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/produtos")
                        .with(csrf())
                        .param("codigo", codigo)
                        .param("nome", "Cadastro não autorizado")
                        .param("tipo", "PRODUTO_MEDICO")
                        .param("unidadeMedida", "UN"))
                .andExpect(status().isForbidden());

        assertFalse(repository.existsByCodigoIgnoreCase(codigo));
    }

    @Test
    @WithMockUser(authorities = "PRODUTO_VISUALIZAR")
    void consultaNaoDevePermitirInativacao() throws Exception {
        mockMvc.perform(post("/produtos/" + produtoId + "/inativar")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(repository.findById(produtoId).orElseThrow().isAtivo());
    }

    @Test
    @WithMockUser(authorities = "PRODUTO_CADASTRAR")
    void devePermitirCadastroComPermissaoEspecifica() throws Exception {
        String codigo = "NOV-" + UUID.randomUUID();

        mockMvc.perform(get("/produtos/novo"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/produtos")
                        .with(csrf())
                        .param("codigo", codigo)
                        .param("nome", "Produto autorizado")
                        .param("tipo", "PRODUTO_MEDICO")
                        .param("unidadeMedida", "UN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("mensagemSucesso"));

        assertTrue(repository.existsByCodigoIgnoreCase(codigo));
    }

    @Test
    @WithMockUser(authorities = "PRODUTO_INATIVAR")
    void devePermitirInativacaoComPermissaoEspecifica() throws Exception {
        mockMvc.perform(post("/produtos/" + produtoId + "/inativar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos/" + produtoId));

        assertFalse(repository.findById(produtoId).orElseThrow().isAtivo());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void nomeDoPerfilNaoDeveSubstituirPermissoes() throws Exception {
        mockMvc.perform(get("/produtos"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/produtos/novo"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/produtos/" + produtoId + "/inativar")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "PRODUTO_TECNICO_EDITAR")
    void edicaoTecnicaNaoDeveLiberarCadastroGeralOuInativacao()
            throws Exception {

        mockMvc.perform(get("/produtos/novo"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/produtos/" + produtoId + "/inativar")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(repository.findById(produtoId).orElseThrow().isAtivo());
    }

    @Test
    @WithMockUser(authorities = {
            "PRODUTO_VISUALIZAR",
            "PRODUTO_CADASTRAR",
            "PRODUTO_INATIVAR"
    })
    void deveNegarOperacaoNaoPrevista() throws Exception {
        mockMvc.perform(post("/produtos/" + produtoId + "/excluir")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(repository.existsById(produtoId));
    }
}