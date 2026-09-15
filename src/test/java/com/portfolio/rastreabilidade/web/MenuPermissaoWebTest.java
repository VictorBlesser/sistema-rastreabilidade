package com.portfolio.rastreabilidade.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuPermissaoWebTest {

    private static final Pattern MENU = Pattern.compile(
            "<nav\\b[^>]*\\baria-label=\"Módulos do sistema\"[^>]*>(.*?)</nav>",
            Pattern.DOTALL);

    private static final Pattern LINK = Pattern.compile(
            "<a\\b[^>]*\\bhref=\"([^\"]*)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails("admin")
    void deveExibirModulosPermitidosAoAdministrador() throws Exception {
        assertEquals(
                List.of(
                        "/",
                        "/produtos",
                        "/usuarios",
                        "/lotes",
                        "/recebimentos",
                        "/estoque",
                        "/expedicoes",
                        "/rastreabilidade"),
                consultarLinksDoMenu());
    }

    @Test
    @WithMockUser(roles = "CONSULTA")
    void consultaSemPermissoesDeveVerSomenteInicio() throws Exception {
        assertEquals(
                List.of("/"),
                consultarLinksDoMenu());
    }

    @Test
    @WithMockUser(authorities = "PRODUTO_VISUALIZAR")
    void deveExibirSomenteInicioEProdutos() throws Exception {
        assertEquals(
                List.of("/", "/produtos"),
                consultarLinksDoMenu());
    }

    @Test
    @WithMockUser(authorities = {
            "LOTE_VISUALIZAR",
            "EXPEDICAO_VISUALIZAR"
    })
    void deveExibirOsDoisModulosAutorizados() throws Exception {
        assertEquals(
                List.of("/", "/lotes", "/expedicoes"),
                consultarLinksDoMenu());
    }

    @Test
    @WithMockUser(authorities = "ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR")
    void quantidadeDisponivelNaoDeveLiberarMenuDeEstoqueFisico()
            throws Exception {

        assertEquals(
                List.of("/"),
                consultarLinksDoMenu());
    }

    private List<String> consultarLinksDoMenu() throws Exception {
        String html = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        Matcher menu = MENU.matcher(html);

        assertTrue(
                menu.find(),
                "A página deve apresentar o menu de módulos.");

        String conteudoMenu = menu.group(1);
        Matcher links = LINK.matcher(conteudoMenu);
        List<String> caminhos = new ArrayList<>();

        while (links.find()) {
            caminhos.add(links.group(1));
        }

        return caminhos;
    }
}