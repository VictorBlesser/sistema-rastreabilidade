package com.portfolio.rastreabilidade.usuario;

import java.util.List;

import com.portfolio.rastreabilidade.acesso.PerfilAcesso;
import com.portfolio.rastreabilidade.acesso.PerfilAcessoRepository;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final PerfilAcessoRepository perfilRepository;
    private final UsuarioPoliticaAcesso politica;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            PerfilAcessoRepository perfilRepository,
            UsuarioPoliticaAcesso politica) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.perfilRepository = perfilRepository;
        this.politica = politica;
    }

    @Transactional
    public Usuario cadastrar(
            String nome,
            String login,
            String senha,
            PerfilUsuario perfil) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }

        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("O login é obrigatório");
        }

        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória");
        }

        if (perfil == null) {
            throw new IllegalArgumentException("O perfil é obrigatório");
        }

        if (repository.existsByLogin(login)) {
            throw new IllegalArgumentException("Login já cadastrado");
        }

        PerfilAcesso perfilAcesso = perfilRepository
                .findByCodigo(perfil.name())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Perfil de acesso não cadastrado"));

        politica.validarCadastro(operadorAtual(), perfilAcesso);

        Usuario usuario = new Usuario(
                nome,
                login,
                passwordEncoder.encode(senha),
                perfil);

        usuario.vincularPerfilInicial(perfilAcesso);

        return repository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll(Sort.by("nome", "id"));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID é obrigatório");
        }

        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado"));
    }

    @Transactional
    public Usuario inativar(Long id) {
        Usuario usuario = buscarPorId(id);

        politica.validarAlteracaoStatus(operadorAtual(), usuario);

        usuario.inativar();
        return repository.save(usuario);
    }

    @Transactional
    public Usuario ativar(Long id) {
        Usuario usuario = buscarPorId(id);

        politica.validarAlteracaoStatus(operadorAtual(), usuario);

        usuario.ativar();
        return repository.save(usuario);
    }

    private Usuario operadorAtual() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException(
                    "É necessário estar autenticado para executar esta operação");
        }

        return repository.buscarComAcessosPorLogin(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException(
                        "Usuário autenticado não encontrado"));
    }
}