package com.portfolio.rastreabilidade.usuario;

import java.util.Set;
import java.util.TreeSet;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login)
            throws UsernameNotFoundException {

        Usuario usuario = repository.buscarComAcessosPorLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado"));

        Set<String> autoridades = new TreeSet<>();

        usuario.getPerfis().forEach(perfil ->
                autoridades.add("ROLE_" + perfil.getCodigo()));

        autoridades.addAll(usuario.getCodigosPermissoes());

        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .authorities(autoridades.toArray(String[]::new))
                .disabled(!usuario.isAtivo())
                .build();
    }
}