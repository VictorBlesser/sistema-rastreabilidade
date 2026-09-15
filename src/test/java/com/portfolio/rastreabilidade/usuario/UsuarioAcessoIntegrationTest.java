package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioAcessoIntegrationTest {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveCarregarPerfilMigradoDoAdministrador() {
        Usuario usuario = repository.buscarComAcessosPorLogin("admin")
                .orElseThrow();

        Set<String> perfis = usuario.getPerfis().stream()
                .map(perfil -> perfil.getCodigo())
                .collect(Collectors.toSet());

        assertEquals(Set.of("ADMINISTRADOR"), perfis);
        assertTrue(usuario.getCodigosPermissoes()
                .contains("USUARIO_CRIAR"));
        assertFalse(usuario.getCodigosPermissoes()
                .contains("PERMISSAO_ADMINISTRADOR_GERENCIAR"));
    }

    @Test
    void deveSomarPermissoesDeComprasEVendas() {
        String login = criarUsuarioDeTeste();

        vincularPerfil(login, "COMPRAS");
        vincularPerfil(login, "VENDAS");

        Usuario usuario = repository.buscarComAcessosPorLogin(login)
                .orElseThrow();

        assertEquals(2, usuario.getPerfis().size());

        assertEquals(
                Set.of(
                        "PRODUTO_VISUALIZAR",
                        "ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR",
                        "ESTOQUE_QUANTIDADE_DISPONIVEL_VISUALIZAR",
                        "CUSTO_VISUALIZAR",
                        "PRECO_VENDA_VISUALIZAR"),
                usuario.getCodigosPermissoes());
    }

    @Test
    void usuarioSemVinculosNaoDeveReceberPermissoes() {
        String login = criarUsuarioDeTeste();

        Usuario usuario = repository.buscarComAcessosPorLogin(login)
                .orElseThrow();

        assertTrue(usuario.getPerfis().isEmpty());
        assertTrue(usuario.getCodigosPermissoes().isEmpty());
    }

    private String criarUsuarioDeTeste() {
        String login = "acesso-" + UUID.randomUUID();

        jdbcTemplate.update("""
                insert into usuario (nome, login, senha_hash, perfil, ativo)
                values (?, ?, ?, ?, ?)
                """,
                "Usuário de teste de acesso",
                login,
                "hash-apenas-para-teste-sem-login",
                "CONSULTA",
                true);

        return login;
    }

    private void vincularPerfil(String login, String perfilCodigo) {
        int inseridos = jdbcTemplate.update("""
                insert into usuario_perfil (usuario_id, perfil_id)
                select u.id, p.id
                from usuario u
                cross join perfil_acesso p
                where u.login = ? and p.codigo = ?
                """,
                login,
                perfilCodigo);

        assertEquals(1, inseridos);
    }
}