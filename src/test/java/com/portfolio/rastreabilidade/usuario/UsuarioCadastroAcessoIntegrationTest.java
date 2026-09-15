package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithUserDetails("admin")
class UsuarioCadastroAcessoIntegrationTest {

    @Autowired
    private UsuarioService service;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void devePersistirUsuarioComPerfilDeAcesso() {
        String login = "compras-" + UUID.randomUUID();

        service.cadastrar(
                "Comprador de teste",
                login,
                "SenhaDeTeste123!",
                PerfilUsuario.COMPRAS);

        entityManager.flush();
        entityManager.clear();

        Usuario salvo = repository.buscarComAcessosPorLogin(login)
                .orElseThrow();

        assertEquals(PerfilUsuario.COMPRAS, salvo.getPerfil());
        assertEquals(1, salvo.getPerfis().size());
        assertEquals(
                "COMPRAS",
                salvo.getPerfis().iterator().next().getCodigo());
        assertTrue(salvo.getCodigosPermissoes().contains("CUSTO_VISUALIZAR"));
    }

    @Test
    void administradorNaoDeveCadastrarOutroAdministrador() {
        String login = "admin-negado-" + UUID.randomUUID();

        assertThrows(
                AccessDeniedException.class,
                () -> service.cadastrar(
                        "Administrador de teste",
                        login,
                        "SenhaDeTeste123!",
                        PerfilUsuario.ADMINISTRADOR));

        assertFalse(repository.existsByLogin(login));
    }

    @Test
    void administradorNaoDeveCadastrarDiretoria() {
        String login = "diretoria-negada-" + UUID.randomUUID();

        assertThrows(
                AccessDeniedException.class,
                () -> service.cadastrar(
                        "Diretoria de teste",
                        login,
                        "SenhaDeTeste123!",
                        PerfilUsuario.DIRETORIA));

        assertFalse(repository.existsByLogin(login));
    }

    @Test
    void administradorNaoDeveInativarProprioUsuario() {
        Usuario admin = repository.findByLogin("admin").orElseThrow();

        assertThrows(
                AccessDeniedException.class,
                () -> service.inativar(admin.getId()));

        entityManager.clear();

        assertTrue(repository.findByLogin("admin").orElseThrow().isAtivo());
    }

    @Test
    void administradorDeveInativarEReativarUsuarioComum() {
        String login = "consulta-status-" + UUID.randomUUID();

        Usuario usuario = service.cadastrar(
                "Consulta de teste",
                login,
                "SenhaDeTeste123!",
                PerfilUsuario.CONSULTA);

        service.inativar(usuario.getId());
        entityManager.flush();
        entityManager.clear();

        assertFalse(repository.findByLogin(login).orElseThrow().isAtivo());

        service.ativar(usuario.getId());
        entityManager.flush();
        entityManager.clear();

        assertTrue(repository.findByLogin(login).orElseThrow().isAtivo());
    }
}