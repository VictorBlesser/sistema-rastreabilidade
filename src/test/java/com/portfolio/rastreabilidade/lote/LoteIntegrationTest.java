package com.portfolio.rastreabilidade.lote;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.produto.TipoProduto;

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
class LoteIntegrationTest {

    @Autowired
    private LoteService service;

    @Autowired
    private LoteRepository repository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MockMvc mockMvc;

    private Produto produto;

    @BeforeEach
    void prepararProduto() {
        produto = produtoRepository.saveAndFlush(new Produto(
                "LOT-" + UUID.randomUUID(),
                "Produto para teste de lote",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true));
    }

    @Test
    void deveCadastrarLoteNormalizado() {
        Lote lote = service.cadastrar(
                produto.getId(),
                " lt-001 ",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1));

        Lote encontrado = service.buscarPorId(lote.getId());

        assertEquals("LT-001", encontrado.getNumero());
        assertEquals(produto.getId(), encontrado.getProduto().getId());
        assertEquals(LocalDate.of(2027, 1, 1), encontrado.getDataValidade());
        assertTrue(encontrado.isAtivo());
    }

    @Test
    void deveRejeitarLoteDuplicadoNoMesmoProduto() {
        service.cadastrar(
                produto.getId(), "LT-001", null, LocalDate.of(2027, 1, 1));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        produto.getId(), " lt-001 ", null,
                        LocalDate.of(2027, 1, 1)));

        assertEquals(
                "Este lote já está cadastrado para o produto",
                erro.getMessage());
    }

    @Test
    void devePermitirMesmoNumeroEmProdutosDiferentes() {
        Produto outro = produtoRepository.saveAndFlush(new Produto(
                "OUT-" + UUID.randomUUID(),
                "Outro produto",
                TipoProduto.PRODUTO_MEDICO,
                true,
                false));

        service.cadastrar(
                produto.getId(), "LT-001", null, LocalDate.of(2027, 1, 1));

        Lote segundo = service.cadastrar(
                outro.getId(), "LT-001", null, null);

        assertEquals(outro.getId(), segundo.getProduto().getId());
    }

    @Test
    void deveExigirValidadeQuandoProdutoControlaValidade() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        produto.getId(), "LT-001", null, null));

        assertEquals(
                "A validade é obrigatória para este produto",
                erro.getMessage());
    }

    @Test
    void deveRejeitarValidadeAnteriorAFabricacao() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        produto.getId(),
                        "LT-001",
                        LocalDate.of(2027, 1, 2),
                        LocalDate.of(2027, 1, 1)));

        assertEquals(
                "A validade não pode ser anterior à fabricação",
                erro.getMessage());
    }

    @Test
    void deveRejeitarProdutoInativo() {
        produto.inativar();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        produto.getId(), "LT-001", null,
                        LocalDate.of(2027, 1, 1)));

        assertEquals("O produto está inativo", erro.getMessage());
    }

    @Test
    void deveRejeitarProdutoSemControleDeLote() {
        Produto semControle = produtoRepository.saveAndFlush(new Produto(
                "SEM-" + UUID.randomUUID(),
                "Produto sem controle",
                TipoProduto.PRODUTO_MEDICO,
                false,
                false));

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        semControle.getId(), "LT-001", null, null));

        assertEquals("O produto não controla lote", erro.getMessage());
    }

    @Test
    @WithMockUser(authorities = "LOTE_CADASTRAR")
    void deveRenderizarFormulario() throws Exception {
        mockMvc.perform(get("/lotes/novo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar lote")))
                .andExpect(content().string(
                        containsString("Produto para teste de lote")));
    }

    @Test
    @WithMockUser(authorities = "LOTE_CADASTRAR")
    void deveCadastrarPelaTela() throws Exception {
        mockMvc.perform(post("/lotes")
                        .with(csrf())
                        .param("produtoId", produto.getId().toString())
                        .param("numero", "LT-TELA")
                        .param("dataFabricacao", "2026-01-01")
                        .param("dataValidade", "2027-01-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/lotes"));

        assertTrue(repository.existsByProdutoIdAndNumero(
                produto.getId(), "LT-TELA"));
    }

    @Test
    @WithMockUser(authorities = "LOTE_VISUALIZAR")
    void deveRenderizarListaEDetalhes() throws Exception {
        Lote lote = service.cadastrar(
                produto.getId(), "LT-DETALHE", null,
                LocalDate.of(2027, 1, 1));

        mockMvc.perform(get("/lotes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("LT-DETALHE")));

        mockMvc.perform(get("/lotes/" + lote.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("LT-DETALHE")))
                .andExpect(content().string(containsString("01/01/2027")));
    }

    @Test
    @WithMockUser(authorities = "LOTE_CADASTRAR")
    void deveExibirErroQuandoValidadeObrigatoriaNaoFoiInformada()
            throws Exception {

        mockMvc.perform(post("/lotes")
                        .with(csrf())
                        .param("produtoId", produto.getId().toString())
                        .param("numero", "LT-SEM-VALIDADE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "A validade é obrigatória para este produto")));
    }

    @Test
    void deveExigirLoginParaAcessarLotes() throws Exception {
        mockMvc.perform(get("/lotes"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(authorities = "LOTE_CADASTRAR")
    void deveRecusarCadastroSemCsrf() throws Exception {
        mockMvc.perform(post("/lotes")
                        .param("produtoId", produto.getId().toString())
                        .param("numero", "LT-SEM-CSRF")
                        .param("dataValidade", "2027-01-01"))
                .andExpect(status().isForbidden());

        assertFalse(repository.existsByProdutoIdAndNumero(
                produto.getId(), "LT-SEM-CSRF"));
    }

    @Test
    @WithMockUser(authorities = "LOTE_VISUALIZAR")
    void consultaNaoDevePermitirCadastro() throws Exception {
        mockMvc.perform(get("/lotes/novo"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/lotes")
                        .with(csrf())
                        .param("produtoId", produto.getId().toString())
                        .param("numero", "LT-NEGADO")
                        .param("dataValidade", "2027-01-01"))
                .andExpect(status().isForbidden());

        assertFalse(repository.existsByProdutoIdAndNumero(
                produto.getId(), "LT-NEGADO"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void nomeDoPerfilNaoDeveSubstituirPermissao() throws Exception {
        mockMvc.perform(get("/lotes"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/lotes/novo"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"LOTE_VISUALIZAR", "LOTE_CADASTRAR"})
    void deveNegarOperacaoNaoPrevista() throws Exception {
        Lote lote = service.cadastrar(
                produto.getId(), "LT-PRESERVADO", null,
                LocalDate.of(2027, 1, 1));

        mockMvc.perform(post("/lotes/" + lote.getId() + "/excluir")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(repository.existsById(lote.getId()));
    }
}