package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void deveCriarUsuarioAtivo() {
        Usuario usuario = new Usuario(
                "Administrador",
                "admin",
                "hash-da-senha",
                PerfilUsuario.ADMINISTRADOR);

        assertTrue(
                usuario.isAtivo(),
                "O usuário deve ser criado como ativo");
    }

    @Test
    void deveInativarUsuario() {
        Usuario usuario = new Usuario(
                "Administrador",
                "admin",
                "hash-da-senha",
                PerfilUsuario.ADMINISTRADOR);

        usuario.inativar();

        assertFalse(
                usuario.isAtivo(),
                "O usuário deve ficar inativo após a inativação");
    }

    @Test
    void deveRejeitarNomeNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        null,
                        "admin",
                        "hash-da-senha",
                        PerfilUsuario.ADMINISTRADOR));
    }

    @Test
    void deveRejeitarNomeVazio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        "",
                        "admin",
                        "hash-da-senha",
                        PerfilUsuario.ADMINISTRADOR));
    }

    @Test
    void deveRejeitarNomeEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        "   ",
                        "admin",
                        "hash-da-senha",
                        PerfilUsuario.ADMINISTRADOR));
    }

    @Test
    void deveRejeitarLoginNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        "Administrador",
                        null,
                        "hash-da-senha",
                        PerfilUsuario.ADMINISTRADOR));
    }

    @Test
    void deveRejeitarLoginVazio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        "Administrador",
                        "",
                        "hash-da-senha",
                        PerfilUsuario.ADMINISTRADOR));
    }

    @Test
    void deveRejeitarLoginEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        "Administrador",
                        "   ",
                        "hash-da-senha",
                        PerfilUsuario.ADMINISTRADOR));
    }

    @Test
    void deveRejeitarSenhaHashNula() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        "Administrador",
                        "admin",
                        null,
                        PerfilUsuario.ADMINISTRADOR));
    }

    @Test
    void deveRejeitarSenhaHashVazia() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        "Administrador",
                        "admin",
                        "",
                        PerfilUsuario.ADMINISTRADOR));
    }

    @Test
    void deveRejeitarSenhaHashEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        "Administrador",
                        "admin",
                        "   ",
                        PerfilUsuario.ADMINISTRADOR));
    }

    @Test
    void deveRejeitarPerfilNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(
                        "Administrador",
                        "admin",
                        "hash-da-senha",
                        null));
    }
}