package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.portfolio.rastreabilidade.config.SenhaConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    private PasswordEncoder passwordEncoder;
    private UsuarioService service;

    @BeforeEach
    void configurar() {
        passwordEncoder = new SenhaConfig().passwordEncoder();
        service = new UsuarioService(repository, passwordEncoder);
    }

    @Test
    void deveCadastrarUsuarioComSenhaHash() {
        when(repository.existsByLogin("admin")).thenReturn(false);
        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Usuario usuario = service.cadastrar(
                "Administrador",
                "admin",
                "SenhaDeTeste123!",
                PerfilUsuario.ADMINISTRADOR);

        assertEquals("Administrador", usuario.getNome());
        assertEquals("admin", usuario.getLogin());
        assertEquals(PerfilUsuario.ADMINISTRADOR, usuario.getPerfil());
        assertTrue(usuario.isAtivo());
        assertNotEquals("SenhaDeTeste123!", usuario.getSenhaHash());
        assertTrue(passwordEncoder.matches(
                "SenhaDeTeste123!",
                usuario.getSenhaHash()));

        verify(repository).save(usuario);
    }

    @Test
    void deveRejeitarLoginDuplicado() {
        when(repository.existsByLogin("admin")).thenReturn(true);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Administrador",
                        "admin",
                        "SenhaDeTeste123!",
                        PerfilUsuario.ADMINISTRADOR));

        assertEquals("Login já cadastrado", erro.getMessage());
        verify(repository, never()).save(any(Usuario.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarNomeInvalido(String nome) {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        nome,
                        "admin",
                        "SenhaDeTeste123!",
                        PerfilUsuario.ADMINISTRADOR));

        assertEquals("O nome é obrigatório", erro.getMessage());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarLoginInvalido(String login) {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Administrador",
                        login,
                        "SenhaDeTeste123!",
                        PerfilUsuario.ADMINISTRADOR));

        assertEquals("O login é obrigatório", erro.getMessage());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarSenhaInvalida(String senha) {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Administrador",
                        "admin",
                        senha,
                        PerfilUsuario.ADMINISTRADOR));

        assertEquals("A senha é obrigatória", erro.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarPerfilNulo() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Administrador",
                        "admin",
                        "SenhaDeTeste123!",
                        null));

        assertEquals("O perfil é obrigatório", erro.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void deveListarUsuariosSolicitandoOrdenacaoPorNomeEId() {
        Usuario ana = new Usuario(
                "Ana",
                "ana",
                "hash-ana",
                PerfilUsuario.ADMINISTRADOR);

        Usuario bruno = new Usuario(
                "Bruno",
                "bruno",
                "hash-bruno",
                PerfilUsuario.CONSULTA);

        bruno.inativar();

        List<Usuario> usuarios = List.of(ana, bruno);
        Sort ordenacao = Sort.by("nome", "id");

        when(repository.findAll(ordenacao)).thenReturn(usuarios);

        List<Usuario> resultado = service.listar();

        assertEquals(usuarios, resultado);
        verify(repository).findAll(ordenacao);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() {
        when(repository.findAll(Sort.by("nome", "id")))
                .thenReturn(List.of());

        assertTrue(service.listar().isEmpty());
    }

    @Test
    void deveBuscarUsuarioPorId() {
        Usuario usuario = new Usuario(
                "Administrador",
                "admin",
                "hash-da-senha",
                PerfilUsuario.ADMINISTRADOR);

        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        Usuario resultado = service.buscarPorId(1L);

        assertSame(usuario, resultado);
        verify(repository).findById(1L);
    }

    @Test
    void deveRejeitarBuscaComIdNulo() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarPorId(null));

        assertEquals("O ID é obrigatório", erro.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarBuscaDeUsuarioInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarPorId(99L));

        assertEquals("Usuário não encontrado", erro.getMessage());
    }

    @Test
    void deveInativarUsuario() {
        Usuario usuario = new Usuario(
                "Administrador",
                "admin",
                "hash-da-senha",
                PerfilUsuario.ADMINISTRADOR);

        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        Usuario resultado = service.inativar(1L);

        assertSame(usuario, resultado);
        assertFalse(resultado.isAtivo());
        verify(repository).save(usuario);
    }

    @Test
    void deveRejeitarInativacaoComIdNulo() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.inativar(null));

        assertEquals("O ID é obrigatório", erro.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void deveRejeitarInativacaoDeUsuarioInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.inativar(99L));

        assertEquals("Usuário não encontrado", erro.getMessage());
        verify(repository, never()).save(any(Usuario.class));
    }
}