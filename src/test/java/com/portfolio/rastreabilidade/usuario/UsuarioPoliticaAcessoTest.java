package com.portfolio.rastreabilidade.usuario;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.portfolio.rastreabilidade.acesso.PerfilAcesso;
import com.portfolio.rastreabilidade.acesso.Permissao;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class UsuarioPoliticaAcessoTest {

    private final UsuarioPoliticaAcesso politica = new UsuarioPoliticaAcesso();

    @Test
    void administradorPodeCadastrarPerfilComPermissoesQuePossui() {
        Usuario operador = usuario(
                1L, "ADMINISTRADOR",
                "USUARIO_CRIAR",
                "USUARIO_VINCULAR_PERFIS",
                "PRODUTO_VISUALIZAR");

        PerfilAcesso destino = perfil("ESTOQUE", "PRODUTO_VISUALIZAR");

        assertDoesNotThrow(() -> politica.validarCadastro(operador, destino));
    }

    @Test
    void administradorNaoPodeCadastrarOutroAdministrador() {
        Usuario operador = usuario(
                1L, "ADMINISTRADOR",
                "USUARIO_CRIAR",
                "USUARIO_VINCULAR_PERFIS");

        assertThrows(
                AccessDeniedException.class,
                () -> politica.validarCadastro(
                        operador, perfil("ADMINISTRADOR")));
    }

    @Test
    void administradorNaoPodeCadastrarDiretoria() {
        Usuario operador = usuario(
                1L, "ADMINISTRADOR",
                "USUARIO_CRIAR",
                "USUARIO_VINCULAR_PERFIS");

        assertThrows(
                AccessDeniedException.class,
                () -> politica.validarCadastro(
                        operador, perfil("DIRETORIA")));
    }

    @Test
    void naoPodeConcederPermissaoQueNaoPossui() {
        Usuario operador = usuario(
                1L, "ADMINISTRADOR",
                "USUARIO_CRIAR",
                "USUARIO_VINCULAR_PERFIS");

        assertThrows(
                AccessDeniedException.class,
                () -> politica.validarCadastro(
                        operador, perfil("COMPRAS", "CUSTO_VISUALIZAR")));
    }

    @Test
    void cadastrarExigePermissaoParaVincularPerfil() {
        Usuario operador = usuario(
                1L, "ADMINISTRADOR", "USUARIO_CRIAR");

        assertThrows(
                AccessDeniedException.class,
                () -> politica.validarCadastro(
                        operador, perfil("CONSULTA")));
    }

    @Test
    void diretoriaPodeCadastrarAdministradorDentroDosSeusAcessos() {
        Usuario operador = usuario(
                1L, "DIRETORIA",
                "USUARIO_CRIAR",
                "USUARIO_VINCULAR_PERFIS",
                "PRODUTO_VISUALIZAR");

        assertDoesNotThrow(() -> politica.validarCadastro(
                operador, perfil("ADMINISTRADOR", "PRODUTO_VISUALIZAR")));
    }

    @Test
    void administradorPodeAlterarStatusDeUsuarioComum() {
        Usuario operador = usuario(
                1L, "ADMINISTRADOR", "USUARIO_ALTERAR_STATUS");

        assertDoesNotThrow(() -> politica.validarAlteracaoStatus(
                operador, usuario(2L, "ESTOQUE")));
    }

    @Test
    void administradorNaoPodeAlterarStatusDeOutroAdministrador() {
        Usuario operador = usuario(
                1L, "ADMINISTRADOR", "USUARIO_ALTERAR_STATUS");

        assertThrows(
                AccessDeniedException.class,
                () -> politica.validarAlteracaoStatus(
                        operador, usuario(2L, "ADMINISTRADOR")));
    }

    @Test
    void diretoriaPodeAlterarStatusDeAdministrador() {
        Usuario operador = usuario(
                1L, "DIRETORIA", "USUARIO_ALTERAR_STATUS");

        assertDoesNotThrow(() -> politica.validarAlteracaoStatus(
                operador, usuario(2L, "ADMINISTRADOR")));
    }

    @Test
    void nemDiretoriaPodeAlterarProprioStatus() {
        Usuario operador = usuario(
                1L, "DIRETORIA", "USUARIO_ALTERAR_STATUS");

        assertThrows(
                AccessDeniedException.class,
                () -> politica.validarAlteracaoStatus(operador, operador));
    }

    @Test
    void perfilSemPermissaoNaoPodeAlterarStatus() {
        Usuario operador = usuario(1L, "ADMINISTRADOR");

        assertThrows(
                AccessDeniedException.class,
                () -> politica.validarAlteracaoStatus(
                        operador, usuario(2L, "ESTOQUE")));
    }

    @Test
    void permissaoSemPerfilDeGestaoNaoPodeAlterarStatus() {
        Usuario operador = usuario(
                1L, "TI", "USUARIO_ALTERAR_STATUS");

        assertThrows(
                AccessDeniedException.class,
                () -> politica.validarAlteracaoStatus(
                        operador, usuario(2L, "ESTOQUE")));
    }

    @Test
    void usuarioInativoNaoPodeAdministrarUsuarios() {
        Usuario operador = usuario(
                1L, "DIRETORIA", "USUARIO_ALTERAR_STATUS");

        when(operador.isAtivo()).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> politica.validarAlteracaoStatus(
                        operador, usuario(2L, "ESTOQUE")));
    }

private Usuario usuario(
        Long id,
        String codigoPerfil,
        String... permissoes) {

    PerfilAcesso perfilAcesso = perfil(codigoPerfil);
    Usuario usuario = mock(Usuario.class);

    when(usuario.getId()).thenReturn(id);
    when(usuario.isAtivo()).thenReturn(true);
    when(usuario.getPerfis()).thenReturn(Set.of(perfilAcesso));
    when(usuario.getCodigosPermissoes()).thenReturn(Set.of(permissoes));

    return usuario;
}

    private PerfilAcesso perfil(String codigo, String... permissoes) {
        PerfilAcesso perfil = mock(PerfilAcesso.class);

        Set<Permissao> acessos = Arrays.stream(permissoes)
                .map(valor -> {
                    Permissao permissao = mock(Permissao.class);
                    when(permissao.getCodigo()).thenReturn(valor);
                    return permissao;
                })
                .collect(Collectors.toSet());

        when(perfil.getCodigo()).thenReturn(codigo);
        when(perfil.getPermissoes()).thenReturn(acessos);

        return perfil;
    }
}