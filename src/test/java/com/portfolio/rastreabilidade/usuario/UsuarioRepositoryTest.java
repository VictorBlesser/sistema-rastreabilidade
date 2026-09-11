package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void deveSalvarEBuscarUsuarioPorLogin() {
        Usuario usuario = new Usuario(
                "Administrador",
                "admin-teste",
                "hash-da-senha",
                PerfilUsuario.ADMINISTRADOR);

        Usuario salvo = repository.saveAndFlush(usuario);

        assertNotNull(salvo.getId());
        Long id = salvo.getId();

        entityManager.clear();

        Usuario encontrado = repository.findByLogin("admin-teste").orElseThrow();

        assertEquals(id, encontrado.getId());
        assertEquals("Administrador", encontrado.getNome());
        assertEquals("admin-teste", encontrado.getLogin());
        assertEquals("hash-da-senha", encontrado.getSenhaHash());
        assertEquals(PerfilUsuario.ADMINISTRADOR, encontrado.getPerfil());
        assertTrue(encontrado.isAtivo());
    }

    @Test
    void deveRetornarVazioQuandoLoginNaoExiste() {
        assertTrue(repository.findByLogin("inexistente").isEmpty());
    }

    @Test
    void deveIdentificarLoginExistente() {
        repository.saveAndFlush(new Usuario(
                "Administrador",
                "admin-teste",
                "hash-da-senha",
                PerfilUsuario.ADMINISTRADOR));

        assertTrue(repository.existsByLogin("admin-teste"));
    }

    @Test
    void deveRetornarFalsoQuandoLoginNaoExiste() {
        assertFalse(repository.existsByLogin("inexistente"));
    }

    @Test
    void deveRejeitarLoginDuplicado() {
        repository.saveAndFlush(new Usuario(
                "Administrador",
                "admin-teste",
                "hash-da-senha",
                PerfilUsuario.ADMINISTRADOR));

        Usuario duplicado = new Usuario(
                "Outro usuário",
                "admin-teste",
                "outro-hash",
                PerfilUsuario.CONSULTA);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(duplicado));
    }

    @Test
    void devePersistirInativacaoDoUsuario() {
        Usuario usuario = repository.saveAndFlush(new Usuario(
                "Administrador",
                "admin-teste",
                "hash-da-senha",
                PerfilUsuario.ADMINISTRADOR));

        Long id = usuario.getId();

        usuario.inativar();
        repository.flush();
        entityManager.clear();

        Usuario encontrado = repository.findById(id).orElseThrow();

        assertFalse(encontrado.isAtivo());
    }
}