package com.portfolio.rastreabilidade;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.produto.TipoProduto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SistemaRastreabilidadeApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ProdutoRepository repository;

	@BeforeEach
	void limparProdutos() {
		repository.deleteAll();
	}

	@Test
	void contextLoads() {
	}

	@Test
	@WithMockUser
	void deveRenderizarListaDeProdutos() throws Exception {
		repository.save(new Produto(
				"CAT-001",
				"Cateter",
				TipoProduto.PRODUTO_MEDICO,
				true,
				true));

		mockMvc.perform(get("/produtos"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Produtos cadastrados")))
				.andExpect(content().string(containsString("CAT-001")))
				.andExpect(content().string(containsString("Produto médico")));
	}

	@Test
	@WithMockUser
	void deveRenderizarFormularioDeProduto() throws Exception {
		mockMvc.perform(get("/produtos/novo"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Cadastrar produto")))
				.andExpect(content().string(containsString("Salvar produto")))
				.andExpect(content().string(containsString("Exige controle de lote")));
	}

	@Test
	@WithMockUser
	void deveRenderizarDetalhesDeProduto() throws Exception {
		Produto produto = repository.save(new Produto(
				"SER-001",
				"Seringa descartável",
				"Seringa estéril para uso único",
				TipoProduto.PRODUTO_MEDICO,
				true,
				false));
		mockMvc.perform(get("/produtos/" + produto.getId()))
				.andExpect(status().isOk())

				.andExpect(content().string(containsString("Seringa descartável")))
				.andExpect(content().string(containsString("SER-001")))
				.andExpect(content().string(containsString("Seringa estéril para uso único")))
				.andExpect(content().string(containsString("Voltar para produtos")))
				.andExpect(content().string(containsString("Inativar produto")));

	}

	@Test
	@WithMockUser
	void deveInativarProdutoPelaTela() throws Exception {
		Produto produto = repository.save(new Produto(
				"CAT-002",
				"Cateter intravenoso",
				TipoProduto.PRODUTO_MEDICO,
				true,
				true));

		mockMvc.perform(post("/produtos/" + produto.getId() + "/inativar")
				.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/produtos/" + produto.getId()));

		Produto produtoAtualizado = repository
				.findById(produto.getId())
				.orElseThrow();

		assertFalse(produtoAtualizado.isAtivo());
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
@WithMockUser
void deveExibirBarraSuperiorNaPaginaInicial() throws Exception {
    mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Sistema genérico")))
            .andExpect(content().string(containsString("Produtos")));
}
}