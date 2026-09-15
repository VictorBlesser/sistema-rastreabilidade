package com.portfolio.rastreabilidade.acesso;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

public interface PerfilAcessoRepository
        extends Repository<PerfilAcesso, Long> {

    @EntityGraph(attributePaths = "permissoes")
    Optional<PerfilAcesso> findById(Long id);

    @EntityGraph(attributePaths = "permissoes")
    Optional<PerfilAcesso> findByCodigo(String codigo);

    @EntityGraph(attributePaths = "permissoes")
    List<PerfilAcesso> findAll(Sort sort);
}