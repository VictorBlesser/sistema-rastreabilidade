package com.portfolio.rastreabilidade.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);

    @EntityGraph(attributePaths = {
            "perfis",
            "perfis.permissoes"
    })
    @Query("select u from Usuario u where u.login = :login")
    Optional<Usuario> buscarComAcessosPorLogin(
            @Param("login") String login);
}