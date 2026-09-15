package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.portfolio.rastreabilidade.acesso.PerfilAcesso;
import com.portfolio.rastreabilidade.acesso.PerfilAcessoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioPermissaoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PerfilAcessoRepository perfilRepository;

    private Usuario usuarioComum;

    @BeforeEach
    void prepararUsuario() {
        usuarioComum = criarUsuarioDeTeste(PerfilUsuario.CONSULTA);
    }

    @Test
    @WithMockUser(authorities = "USUARIO_VISUALIZAR")
    void permissaoDeConsultaDeveLiberarListaEDetalhes() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/usuarios/{id}", usuarioComum.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USUARIO_VISUALIZAR")
    void consultaNaoDeveLiberarCadastroOuAlteracaoStatus() throws Exception {
        mockMvc.perform(get("/usuarios/novo"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/usuarios").with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(
                        "/usuarios/{id}/inativar", usuarioComum.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(
                        "/usuarios/{id}/ativar", usuarioComum.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(repository.findById(usuarioComum.getId())
                .orElseThrow().isAtivo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMINISTRADOR", "DIRETORIA"})
    void perfilSemPermissoesNaoDeveLiberarRotas(String perfil)
            throws Exception {

        mockMvc.perform(get("/usuarios")
                        .with(user("gestor").roles(perfil)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/usuarios/novo")
                        .with(user("gestor").roles(perfil)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(
                        "/usuarios/{id}/inativar", usuarioComum.getId())
                        .with(user("gestor").roles(perfil))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {
            "USUARIO_CRIAR",
            "USUARIO_VINCULAR_PERFIS",
            "USUARIO_ALTERAR_STATUS"
    })
    void permissoesSemPerfilDeGestaoNaoDevemLiberarEscrita()
            throws Exception {

        mockMvc.perform(get("/usuarios/novo"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/usuarios").with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(
                        "/usuarios/{id}/inativar", usuarioComum.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {
            "ROLE_ADMINISTRADOR",
            "USUARIO_CRIAR"
    })
    void cadastroDeveExigirPermissaoDeVincularPerfil() throws Exception {
        mockMvc.perform(get("/usuarios/novo"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/usuarios").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin")
    void administradorDeveCadastrarUsuarioComum() throws Exception {
        String login = "novo-" + UUID.randomUUID();

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .param("nome", "Usuário comum")
                        .param("login", login)
                        .param("senha", "SenhaDeTeste123!")
                        .param("perfil", "CONSULTA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        assertTrue(repository.existsByLogin(login));
    }

    @Test
    @WithUserDetails("admin")
    void administradorNaoDeveCadastrarAdministrador() throws Exception {
        String login = "negado-" + UUID.randomUUID();

        mockMvc.perform(post("/usuarios")
                        .with(csrf())
                        .param("nome", "Administrador")
                        .param("login", login)
                        .param("senha", "SenhaDeTeste123!")
                        .param("perfil", "ADMINISTRADOR"))
                .andExpect(status().isForbidden());

        assertFalse(repository.existsByLogin(login));
    }

    @Test
    void diretoriaDeveCadastrarAdministrador() throws Exception {
        Usuario diretoria = criarUsuarioDeTeste(PerfilUsuario.DIRETORIA);
        String login = "gestor-" + UUID.randomUUID();

        mockMvc.perform(post("/usuarios")
                        .with(user(diretoria.getLogin()).authorities(
                                AuthorityUtils.createAuthorityList(
                                        "ROLE_DIRETORIA",
                                        "USUARIO_CRIAR",
                                        "USUARIO_VINCULAR_PERFIS")))
                        .with(csrf())
                        .param("nome", "Administrador autorizado")
                        .param("login", login)
                        .param("senha", "SenhaDeTeste123!")
                        .param("perfil", "ADMINISTRADOR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        assertTrue(repository.existsByLogin(login));
    }

    @Test
    @WithUserDetails("admin")
    void cadastroSemCsrfDeveSerNegado() throws Exception {
        String login = "csrf-" + UUID.randomUUID();

        mockMvc.perform(post("/usuarios")
                        .param("nome", "Usuário sem CSRF")
                        .param("login", login)
                        .param("senha", "SenhaDeTeste123!")
                        .param("perfil", "CONSULTA"))
                .andExpect(status().isForbidden());

        assertFalse(repository.existsByLogin(login));
    }

    @Test
    @WithUserDetails("admin")
    void rotaDeExclusaoDeveSerNegada() throws Exception {
        mockMvc.perform(post(
                        "/usuarios/{id}/excluir", usuarioComum.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(repository.existsById(usuarioComum.getId()));
    }

    private Usuario criarUsuarioDeTeste(PerfilUsuario perfil) {
        PerfilAcesso perfilAcesso = perfilRepository
                .findByCodigo(perfil.name())
                .orElseThrow();

        Usuario usuario = new Usuario(
                "Usuário de teste",
                "web-" + UUID.randomUUID(),
                "hash-exclusivo-de-teste",
                perfil);

        usuario.vincularPerfilInicial(perfilAcesso);

        return repository.saveAndFlush(usuario);
    }
}