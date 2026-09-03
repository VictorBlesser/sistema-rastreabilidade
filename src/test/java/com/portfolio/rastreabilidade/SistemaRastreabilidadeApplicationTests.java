package com.portfolio.rastreabilidade;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

}
