package com.portfolio.rastreabilidade.usuario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.portfolio.rastreabilidade.acesso.PerfilAcesso;
import com.portfolio.rastreabilidade.acesso.PerfilAcessoRepository;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioAcessoTelaService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilAcessoRepository perfilRepository;
    private final UsuarioPoliticaAcesso politica;

    public UsuarioAcessoTelaService(
            UsuarioRepository usuarioRepository,
            PerfilAcessoRepository perfilRepository,
            UsuarioPoliticaAcesso politica) {

        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.politica = politica;
    }

    public List<PerfilUsuario> perfisPermitidos() {
        Usuario operador = operadorAutorizado(
                "USUARIO_CRIAR", "USUARIO_VINCULAR_PERFIS");

        if (operador == null) {
            return List.of();
        }

        List<PerfilUsuario> permitidos = new ArrayList<>();

        for (PerfilAcesso perfil : perfilRepository.findAll(Sort.by("codigo"))) {
            PerfilUsuario opcao = Arrays.stream(PerfilUsuario.values())
                    .filter(valor -> valor.name().equals(perfil.getCodigo()))
                    .findFirst()
                    .orElse(null);

            if (opcao == null) {
                continue;
            }

            try {
                politica.validarCadastro(operador, perfil);
                permitidos.add(opcao);
            } catch (AccessDeniedException ignorada) {
                // Perfis não autorizados não são oferecidos no formulário.
            }
        }

        return List.copyOf(permitidos);
    }

    public boolean podeAlterarStatus(Long usuarioId) {
        Usuario operador = operadorAutorizado("USUARIO_ALTERAR_STATUS");

        if (operador == null || usuarioId == null) {
            return false;
        }

        Usuario destino = usuarioRepository.findById(usuarioId).orElse(null);

        if (destino == null) {
            return false;
        }

        try {
            politica.validarAlteracaoStatus(operador, destino);
            return true;
        } catch (AccessDeniedException ignorada) {
            return false;
        }
    }

    private Usuario operadorAutorizado(String... permissoes) {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Set<String> autoridades = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean gestor = autoridades.contains("ROLE_DIRETORIA")
                || autoridades.contains("ROLE_ADMINISTRADOR");

        if (!gestor || !autoridades.containsAll(Arrays.asList(permissoes))) {
            return null;
        }

        return usuarioRepository
                .buscarComAcessosPorLogin(authentication.getName())
                .filter(Usuario::isAtivo)
                .orElse(null);
    }
}