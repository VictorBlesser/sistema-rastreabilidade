package com.portfolio.rastreabilidade.acesso;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

public interface PermissaoRepository extends Repository<Permissao, Long> {

    Optional<Permissao> findById(Long id);

    Optional<Permissao> findByCodigo(String codigo);

    List<Permissao> findAll(Sort sort);
}