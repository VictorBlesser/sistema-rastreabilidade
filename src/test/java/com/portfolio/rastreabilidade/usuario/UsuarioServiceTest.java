package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.portfolio.rastreabilidade.acesso.PerfilAcesso;
import com.portfolio.rastreabilidade.acesso.PerfilAcessoRepository;
import com.portfolio.rastreabilidade.config.SenhaConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PerfilAcessoRepository perfilRepository;

    @Mock
    private PerfilAcesso perfilAcesso;

    @Mock
    private UsuarioPoliticaAcesso politica;

    @Mock
    private Usuario operador;

    private PasswordEncoder passwordEncoder;
    private UsuarioService service;

    @BeforeEach
    void configurar() {
        SecurityContextHolder.clearContext();

        passwordEncoder = new SenhaConfig().passwordEncoder();

        service = new UsuarioService(
                repository, passwordEncoder, perfilRepository, politica);
    }

    @AfterEach
    void limparAutenticacao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveCadastrarUsuarioComSenhaHashEPerfilVinculado() {
        autenticarOperador();

        when(perfilRepository.findByCodigo("CONSULTA"))
                .thenReturn(Optional.of(perfilAcesso));
        when(perfilAcesso.getId()).thenReturn(1L);
        when(perfilAcesso.getCodigo()).thenReturn("CONSULTA");
        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        Usuario usuario = service.cadastrar(
                "Consulta",
                "consulta",
                "SenhaDeTeste123!",
                PerfilUsuario.CONSULTA);

        assertEquals("Consulta", usuario.getNome());
        assertEquals("consulta", usuario.getLogin());
        assertEquals(PerfilUsuario.CONSULTA, usuario.getPerfil());
        assertTrue(usuario.isAtivo());
        assertNotEquals("SenhaDeTeste123!", usuario.getSenhaHash());
        assertTrue(passwordEncoder.matches(
                "SenhaDeTeste123!", usuario.getSenhaHash()));
        assertEquals(1, usuario.getPerfis().size());
        assertTrue(usuario.getPerfis().contains(perfilAcesso));

        verify(politica).validarCadastro(operador, perfilAcesso);
        verify(repository).save(usuario);
    }

    @Test
    void cadastroNegadoNaoDeveSalvarUsuario() {
        autenticarOperador();

        when(perfilRepository.findByCodigo("CONSULTA"))
                .thenReturn(Optional.of(perfilAcesso));

        doThrow(new AccessDeniedException("Cadastro negado"))
                .when(politica)
                .validarCadastro(operador, perfilAcesso);

        assertThrows(
                AccessDeniedException.class,
                () -> service.cadastrar(
                        "Consulta", "consulta", "Senha123!",
                        PerfilUsuario.CONSULTA));

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void cadastroSemAutenticacaoDeveSerNegado() {
        when(perfilRepository.findByCodigo("CONSULTA"))
                .thenReturn(Optional.of(perfilAcesso));

        assertThrows(
                AccessDeniedException.class,
                () -> service.cadastrar(
                        "Consulta", "consulta", "Senha123!",
                        PerfilUsuario.CONSULTA));

        verify(repository, never()).save(any(Usuario.class));
        verifyNoInteractions(politica);
    }

    @Test
    void deveRejeitarPerfilSemCadastroNoBanco() {
        when(perfilRepository.findByCodigo("CONSULTA"))
                .thenReturn(Optional.empty());

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Consulta", "consulta", "Senha123!",
                        PerfilUsuario.CONSULTA));

        assertEquals("Perfil de acesso não cadastrado", erro.getMessage());
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void deveRejeitarLoginDuplicado() {
        when(repository.existsByLogin("admin")).thenReturn(true);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Administrador", "admin", "Senha123!",
                        PerfilUsuario.ADMINISTRADOR));

        assertEquals("Login já cadastrado", erro.getMessage());
        verify(repository, never()).save(any(Usuario.class));
        verifyNoInteractions(perfilRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarNomeInvalido(String nome) {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        nome, "admin", "Senha123!",
                        PerfilUsuario.ADMINISTRADOR));

        assertEquals("O nome é obrigatório", erro.getMessage());
        verifyNoInteractions(repository, perfilRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarLoginInvalido(String login) {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Administrador", login, "Senha123!",
                        PerfilUsuario.ADMINISTRADOR));

        assertEquals("O login é obrigatório", erro.getMessage());
        verifyNoInteractions(repository, perfilRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void deveRejeitarSenhaInvalida(String senha) {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Administrador", "admin", senha,
                        PerfilUsuario.ADMINISTRADOR));

        assertEquals("A senha é obrigatória", erro.getMessage());
        verifyNoInteractions(repository, perfilRepository);
    }

    @Test
    void deveRejeitarPerfilNulo() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(
                        "Administrador", "admin", "Senha123!", null));

        assertEquals("O perfil é obrigatório", erro.getMessage());
        verifyNoInteractions(repository, perfilRepository);
    }

    @Test
    void deveListarUsuariosSolicitandoOrdenacaoPorNomeEId() {
        Usuario ana = usuario("ana", PerfilUsuario.ADMINISTRADOR);
        Usuario bruno = usuario("bruno", PerfilUsuario.CONSULTA);
        bruno.inativar();

        List<Usuario> usuarios = List.of(ana, bruno);
        Sort ordenacao = Sort.by("nome", "id");

        when(repository.findAll(ordenacao)).thenReturn(usuarios);

        assertEquals(usuarios, service.listar());
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
        Usuario usuario = usuario("admin", PerfilUsuario.ADMINISTRADOR);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        assertSame(usuario, service.buscarPorId(1L));
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
        autenticarOperador();

        Usuario usuario = usuario("consulta", PerfilUsuario.CONSULTA);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        Usuario resultado = service.inativar(1L);

        assertSame(usuario, resultado);
        assertFalse(resultado.isAtivo());
        verify(politica).validarAlteracaoStatus(operador, usuario);
        verify(repository).save(usuario);
    }

    @Test
    void deveAtivarUsuario() {
        autenticarOperador();

        Usuario usuario = usuario("consulta", PerfilUsuario.CONSULTA);
        usuario.inativar();

        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        Usuario resultado = service.ativar(1L);

        assertSame(usuario, resultado);
        assertTrue(resultado.isAtivo());
        verify(politica).validarAlteracaoStatus(operador, usuario);
        verify(repository).save(usuario);
    }

    @Test
    void inativacaoNegadaDevePreservarStatus() {
        autenticarOperador();

        Usuario usuario = usuario("consulta", PerfilUsuario.CONSULTA);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        doThrow(new AccessDeniedException("Alteração negada"))
                .when(politica)
                .validarAlteracaoStatus(operador, usuario);

        assertThrows(
                AccessDeniedException.class,
                () -> service.inativar(1L));

        assertTrue(usuario.isAtivo());
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void ativacaoNegadaDevePreservarStatus() {
        autenticarOperador();

        Usuario usuario = usuario("consulta", PerfilUsuario.CONSULTA);
        usuario.inativar();

        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        doThrow(new AccessDeniedException("Alteração negada"))
                .when(politica)
                .validarAlteracaoStatus(operador, usuario);

        assertThrows(
                AccessDeniedException.class,
                () -> service.ativar(1L));

        assertFalse(usuario.isAtivo());
        verify(repository, never()).save(any(Usuario.class));
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

    private void autenticarOperador() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "operador-teste", null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(repository.buscarComAcessosPorLogin("operador-teste"))
                .thenReturn(Optional.of(operador));
    }

    private Usuario usuario(String login, PerfilUsuario perfil) {
        return new Usuario(login, login, "hash-de-teste", perfil);
    }
}