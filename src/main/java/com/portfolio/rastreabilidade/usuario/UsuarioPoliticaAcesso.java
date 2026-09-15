package com.portfolio.rastreabilidade.usuario;

import java.util.Set;
import java.util.stream.Collectors;

import com.portfolio.rastreabilidade.acesso.PerfilAcesso;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class UsuarioPoliticaAcesso {

    public void validarCadastro(Usuario operador, PerfilAcesso perfilDestino) {
        exigirOperador(operador, "USUARIO_CRIAR");
        exigirPermissao(operador, "USUARIO_VINCULAR_PERFIS");

        if (perfilDestino == null) {
            throw new IllegalArgumentException("O perfil é obrigatório");
        }

        validarHierarquia(
                operador,
                Set.of(perfilDestino.getCodigo()));

        Set<String> permissoesDestino = perfilDestino.getPermissoes().stream()
                .map(permissao -> permissao.getCodigo())
                .collect(Collectors.toSet());

        if (!operador.getCodigosPermissoes().containsAll(permissoesDestino)) {
            negar("Não é permitido conceder permissões que você não possui");
        }
    }

    public void validarAlteracaoStatus(Usuario operador, Usuario destino) {
        exigirOperador(operador, "USUARIO_ALTERAR_STATUS");

        if (destino == null || destino.getId() == null) {
            throw new IllegalArgumentException("Usuário de destino inválido");
        }

        if (operador.getId().equals(destino.getId())) {
            negar("Não é permitido alterar o próprio status");
        }

        Set<String> perfisDestino = destino.getPerfis().stream()
                .map(PerfilAcesso::getCodigo)
                .collect(Collectors.toSet());

        if (destino.getPerfil() != null) {
            perfisDestino.add(destino.getPerfil().name());
        }

        validarHierarquia(operador, perfisDestino);
    }

    private void exigirOperador(Usuario operador, String permissao) {
        if (operador == null
                || operador.getId() == null
                || !operador.isAtivo()) {
            negar("É necessário um usuário ativo para executar esta operação");
        }

        if (!possuiPerfil(operador, "DIRETORIA")
                && !possuiPerfil(operador, "ADMINISTRADOR")) {
            negar("Esta operação exige Diretoria ou Administrador");
        }

        exigirPermissao(operador, permissao);
    }

    private void exigirPermissao(Usuario operador, String permissao) {
        if (!operador.getCodigosPermissoes().contains(permissao)) {
            negar("Você não possui permissão para executar esta operação");
        }
    }

    private void validarHierarquia(
            Usuario operador,
            Set<String> perfisDestino) {

        boolean destinoProtegido = perfisDestino.contains("DIRETORIA")
                || perfisDestino.contains("ADMINISTRADOR");

        if (destinoProtegido && !possuiPerfil(operador, "DIRETORIA")) {
            negar("Somente a Diretoria pode administrar este perfil");
        }
    }

    private boolean possuiPerfil(Usuario usuario, String codigo) {
        return usuario.getPerfis().stream()
                .anyMatch(perfil -> codigo.equals(perfil.getCodigo()));
    }

    private void negar(String mensagem) {
        throw new AccessDeniedException(mensagem);
    }
}