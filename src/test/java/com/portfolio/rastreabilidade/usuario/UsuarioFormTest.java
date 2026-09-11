package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UsuarioFormTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    private UsuarioForm form;

    @BeforeAll
    static void iniciarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fecharValidador() {
        factory.close();
    }

    @BeforeEach
    void prepararFormulario() {
        form = new UsuarioForm();
        form.setNome("Administrador");
        form.setLogin("admin");
        form.setSenha("SenhaDeTeste123!");
        form.setPerfil(PerfilUsuario.ADMINISTRADOR);
    }

    @Test
    void deveAceitarFormularioValido() {
        assertTrue(validator.validate(form).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarNomeInvalido(String nome) {
        form.setNome(nome);

        assertEquals(1, validator.validateProperty(form, "nome").size());
    }

    @Test
    void deveRejeitarNomeAcimaDoLimite() {
        form.setNome("a".repeat(151));

        assertEquals(1, validator.validateProperty(form, "nome").size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarLoginInvalido(String login) {
        form.setLogin(login);

        assertEquals(1, validator.validateProperty(form, "login").size());
    }

    @Test
    void deveRejeitarLoginAcimaDoLimite() {
        form.setLogin("a".repeat(101));

        assertEquals(1, validator.validateProperty(form, "login").size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarSenhaInvalida(String senha) {
        form.setSenha(senha);

        assertEquals(1, validator.validateProperty(form, "senha").size());
    }

    @Test
    void deveRejeitarPerfilNulo() {
        form.setPerfil(null);

        assertEquals(1, validator.validateProperty(form, "perfil").size());
    }

    @Test
    void deveAceitarNomeELoginNoLimite() {
        form.setNome("a".repeat(150));
        form.setLogin("a".repeat(100));

        assertTrue(validator.validate(form).isEmpty());
    }
}