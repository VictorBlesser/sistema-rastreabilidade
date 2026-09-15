package com.portfolio.rastreabilidade;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.produto.TipoProduto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SistemaRastreabilidadeApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository repository;

    @Test
    void contextLoads() {
    }

       @Test
    @WithUserDetails("admin")
    void deveRenderizarListaDeProdutos() throws Exception {
        String codigo = "CAT-" + UUID.randomUUID();

        repository.saveAndFlush(new Produto(
                codigo,
                "Cateter",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Produtos cadastrados")))
                .andExpect(content().string(
                        containsString(codigo.toUpperCase(java.util.Locale.ROOT))))
                .andExpect(content().string(containsString("Produto médico")));
    }

    @Test
    @WithUserDetails("admin")
    void deveRenderizarDetalhesDeProduto() throws Exception {
        String codigo = "SER-" + UUID.randomUUID();

        Produto produto = repository.saveAndFlush(new Produto(
                codigo,
                "Seringa descartável",
                "Seringa estéril para uso único",
                TipoProduto.PRODUTO_MEDICO,
                true,
                false));

        mockMvc.perform(get("/produtos/" + produto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Seringa descartável")))
                .andExpect(content().string(
                        containsString(codigo.toUpperCase(java.util.Locale.ROOT))))
                .andExpect(content().string(
                        containsString("Seringa estéril para uso único")))
                .andExpect(content().string(containsString("Voltar para produtos")))
                .andExpect(content().string(containsString("Inativar produto")));
    }

    @Test
    @WithUserDetails("admin")
    void deveInativarProdutoPelaTela() throws Exception {
        Produto produto = repository.saveAndFlush(new Produto(
                "CAT-" + UUID.randomUUID(),
                "Cateter intravenoso",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true));

        mockMvc.perform(post("/produtos/" + produto.getId() + "/inativar")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos/" + produto.getId()));

        assertFalse(repository.findById(produto.getId())
                .orElseThrow()
                .isAtivo());
    }

    @Test
    void deveExibirPaginaDeLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Acessar o sistema")))
                .andExpect(content().string(containsString("name=\"username\"")))
                .andExpect(content().string(containsString("name=\"password\"")));
    }

    @Test
    void deveRedirecionarUsuarioNaoAutenticadoParaLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithUserDetails("admin")
    void deveExibirBarraSuperiorNaPaginaInicial() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sistema genérico")))
                .andExpect(content().string(containsString("Produtos")));
    }
}