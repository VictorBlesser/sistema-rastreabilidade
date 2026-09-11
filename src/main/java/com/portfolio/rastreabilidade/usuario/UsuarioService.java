package com.portfolio.rastreabilidade.usuario;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
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

        String senhaHash = passwordEncoder.encode(senha);

        Usuario usuario = new Usuario(
                nome,
                login,
                senhaHash,
                perfil);

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
                .orElseThrow(
                        () -> new IllegalArgumentException("Usuário não encontrado"));
    }

    @Transactional
public Usuario inativar(Long id) {
    Usuario usuario = buscarPorId(id);

    if (usuario.getPerfil() == PerfilUsuario.ADMINISTRADOR) {
        throw new IllegalArgumentException(
                "Usuários administradores não podem ser inativados");
    }

    usuario.inativar();

    return repository.save(usuario);
}

@Transactional
public Usuario ativar(Long id) {
    Usuario usuario = buscarPorId(id);

    if (usuario.getPerfil() == PerfilUsuario.ADMINISTRADOR) {
        throw new IllegalArgumentException(
                "Usuários administradores não podem ser reativados por esta ação");
    }

    usuario.ativar();

    return repository.save(usuario);
}
}