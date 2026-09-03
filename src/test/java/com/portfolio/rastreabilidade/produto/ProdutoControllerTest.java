package com.portfolio.rastreabilidade.produto;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class ProdutoControllerTest {

    @Mock
    private ProdutoService service;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProdutoController(service))
                .setValidator(validator)
                .build();
    }

    @Test
    void deveListarProdutos() throws Exception {
        Produto produto = novoProduto();
        when(service.listar()).thenReturn(List.of(produto));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/lista"))
                .andExpect(model().attribute("produtos", hasSize(1)));
    }

    @Test
    void deveExibirFormularioDeCadastro() throws Exception {
        mockMvc.perform(get("/produtos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/formulario"))
                .andExpect(model().attributeExists("produtoForm"))
                .andExpect(model().attribute("tiposProduto", arrayContaining(TipoProduto.values())));
    }

    @Test
    void deveCadastrarProdutoValido() throws Exception {
        when(service.cadastrar(any(Produto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/produtos")
                        .param("codigo", " cat-001 ")
                        .param("nome", " Cateter ")
                        .param("descricao", " Uso hospitalar ")
                        .param("tipo", "PRODUTO_MEDICO")
                        .param("controlaLote", "true")
                        .param("controlaValidade", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/produtos"))
                .andExpect(flash().attribute("mensagemSucesso", "Produto cadastrado com sucesso"));

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        verify(service).cadastrar(captor.capture());
        assertEquals("CAT-001", captor.getValue().getCodigo());
        assertEquals("Cateter", captor.getValue().getNome());
        assertEquals("Uso hospitalar", captor.getValue().getDescricao());
    }

    @Test
    void deveRecusarFormularioInvalido() throws Exception {
        mockMvc.perform(post("/produtos")
                        .param("codigo", " ")
                        .param("nome", " "))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/formulario"))
                .andExpect(model().attributeHasFieldErrors("produtoForm", "codigo", "nome", "tipo"))
                .andExpect(model().attributeExists("tiposProduto"));

        verify(service, never()).cadastrar(any());
    }

    @Test
    void deveExibirErroQuandoCodigoJaExiste() throws Exception {
        when(service.cadastrar(any(Produto.class)))
                .thenThrow(new IllegalArgumentException("Código já cadastrado"));

        mockMvc.perform(post("/produtos")
                        .param("codigo", "CAT-001")
                        .param("nome", "Cateter")
                        .param("tipo", "PRODUTO_MEDICO"))
                .andExpect(status().isOk())
                .andExpect(view().name("produtos/formulario"))
                .andExpect(model().attributeHasFieldErrorCode(
                        "produtoForm",
                        "codigo",
                        "produto.codigo.duplicado"))
                .andExpect(model().attributeExists("tiposProduto"));
    }

    private Produto novoProduto() {
        return new Produto("CAT-001", "Cateter", TipoProduto.PRODUTO_MEDICO, true, true);
    }
}
