package com.portfolio.rastreabilidade.produto;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarParaMovimentacao(@Param("id") Long id);
}