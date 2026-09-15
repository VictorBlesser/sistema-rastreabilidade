package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithUserDetails("admin")
class UsuarioDetailsServiceIntegrationTest {

    @Autowired
    private UsuarioDetailsService detailsService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveCarregarAdministradorComPermissoesPermitidas() {
        UserDetails detalhes = detailsService.loadUserByUsername("admin");
        Set<String> autoridades = autoridades(detalhes);

        assertTrue(detalhes.isEnabled());
        assertTrue(autoridades.contains("ROLE_ADMINISTRADOR"));
        assertTrue(autoridades.contains("USUARIO_CRIAR"));
        assertTrue(autoridades.contains("PERMISSAO_GERENCIAR"));

        assertFalse(autoridades.contains("ROLE_DIRETORIA"));
        assertFalse(autoridades.contains(
                "PERMISSAO_ADMINISTRADOR_GERENCIAR"));
    }

    @Test
    void deveUnirPerfisEPermissoesSemDuplicacao() {
        String login = criarUsuario(PerfilUsuario.COMPRAS);

        int inseridos = jdbcTemplate.update("""
                insert into usuario_perfil (usuario_id, perfil_id)
                select u.id, p.id
                from usuario u
                cross join perfil_acesso p
                where u.login = ? and p.codigo = ?
                """,
                login,
                "VENDAS");

        assertEquals(1, inseridos);
        entityManager.clear();

        UserDetails detalhes = detailsService.loadUserByUsername(login);

        assertEquals(
                Set.of(
                        "ROLE_COMPRAS",
                        "ROLE_VENDAS",
                        "PRODUTO_VISUALIZAR",
                        "ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR",
                        "ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR",
                        "CUSTO_VISUALIZAR",
                        "PRECO_VENDA_VISUALIZAR"),
                autoridades(detalhes));

        assertEquals(7, detalhes.getAuthorities().size());
    }

    @Test
    void consultaDeveReceberPerfilSemPermissoesOperacionais() {
        String login = criarUsuario(PerfilUsuario.CONSULTA);

        UserDetails detalhes = detailsService.loadUserByUsername(login);

        assertEquals(Set.of("ROLE_CONSULTA"), autoridades(detalhes));
    }

    @Test
    void usuarioSemVinculosNaoDeveUsarPerfilAntigoComoAlternativa() {
        String login = criarUsuario(PerfilUsuario.COMPRAS);

        jdbcTemplate.update("""
                delete from usuario_perfil
                where usuario_id = (
                    select id from usuario where login = ?
                )
                """,
                login);

        entityManager.clear();

        UserDetails detalhes = detailsService.loadUserByUsername(login);

        assertTrue(detalhes.getAuthorities().isEmpty());
    }

    @Test
    void deveMarcarUsuarioInativoComoDesabilitado() {
        String login = criarUsuario(PerfilUsuario.CONSULTA);

        jdbcTemplate.update(
                "update usuario set ativo = false where login = ?",
                login);

        entityManager.clear();

        UserDetails detalhes = detailsService.loadUserByUsername(login);

        assertFalse(detalhes.isEnabled());
    }

    @Test
    void deveRecusarUsuarioInexistente() {
        assertThrows(
                UsernameNotFoundException.class,
                () -> detailsService.loadUserByUsername(
                        "inexistente-" + UUID.randomUUID()));
    }

    private String criarUsuario(PerfilUsuario perfil) {
        String login = "login-teste-" + UUID.randomUUID();

        usuarioService.cadastrar(
                "Usuário de teste de autenticação",
                login,
                "SenhaDeTeste123!",
                perfil);

        entityManager.flush();
        entityManager.clear();

        return login;
    }

    private Set<String> autoridades(UserDetails detalhes) {
        return detalhes.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}