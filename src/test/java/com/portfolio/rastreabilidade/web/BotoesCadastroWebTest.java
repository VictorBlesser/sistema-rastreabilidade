package com.portfolio.rastreabilidade.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import com.portfolio.rastreabilidade.lote.LoteService;
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
class BotoesCadastroWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private LoteService loteService;

    private Produto produto;
    private Long loteId;

    @BeforeEach
    void preparar() {
        produto = produtoRepository.saveAndFlush(new Produto(
                "UI-" + UUID.randomUUID(),
                "Produto para verificar permissões da interface",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true));

        loteId = loteService.cadastrar(
                produto.getId(),
                "LOTE-UI",
                null,
                LocalDate.now().plusYears(1)).getId();
    }

    @Test
    @WithMockUser(authorities = "PRODUTO_VISUALIZAR")
    void consultaDeProdutoNaoDeveExibirCadastroNemInativacao()
            throws Exception {

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        not(containsString("href=\"/produtos/novo\""))));

        mockMvc.perform(get("/produtos/{id}", produto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(produto.getNome())))
                .andExpect(content().string(
                        not(containsString(actionInativar()))));
    }

    @Test
    @WithMockUser(authorities = {
            "PRODUTO_VISUALIZAR",
            "PRODUTO_CADASTRAR",
            "PRODUTO_INATIVAR"
    })
    void deveExibirCadastroEInativacaoComPermissoes()
            throws Exception {

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("href=\"/produtos/novo\"")));

        mockMvc.perform(get("/produtos/{id}", produto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString(actionInativar())));
    }

    @Test
    @WithMockUser(authorities = {
            "PRODUTO_VISUALIZAR",
            "PRODUTO_INATIVAR"
    })
    void produtoInativoNaoDeveOferecerInativacao()
            throws Exception {

        produto.inativar();
        produtoRepository.saveAndFlush(produto);

        mockMvc.perform(get("/produtos/{id}", produto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        not(containsString(actionInativar()))));
    }

    @Test
    @WithMockUser(authorities = "LOTE_VISUALIZAR")
    void consultaDeLoteNaoDeveExibirCadastroNemLinksRestritos()
            throws Exception {

        mockMvc.perform(get("/lotes"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        not(containsString("href=\"/lotes/novo\""))));

        mockMvc.perform(get("/lotes/{id}", loteId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(produto.getNome())))
                .andExpect(content().string(
                        not(containsString(linkProduto()))))
                .andExpect(content().string(
                        not(containsString(linkHistorico()))));
    }

    @Test
    @WithMockUser(authorities = {
            "LOTE_VISUALIZAR",
            "LOTE_CADASTRAR",
            "PRODUTO_VISUALIZAR",
            "RASTREABILIDADE_VISUALIZAR"
    })
    void deveExibirCadastroDeLoteELinksAutorizados()
            throws Exception {

        mockMvc.perform(get("/lotes"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("href=\"/lotes/novo\"")));

        mockMvc.perform(get("/lotes/{id}", loteId))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString(linkProduto())))
                .andExpect(content().string(
                        containsString(linkHistorico())));
    }

    private String actionInativar() {
        return "action=\"/produtos/" + produto.getId() + "/inativar\"";
    }

    private String linkProduto() {
        return "href=\"/produtos/" + produto.getId() + "\"";
    }

    private String linkHistorico() {
        return "href=\"/rastreabilidade?loteId=" + loteId + "\"";
    }
}