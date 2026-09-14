package com.portfolio.rastreabilidade.expedicao;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ExpedicaoRepository
        extends JpaRepository<Expedicao, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "itens",
            "itens.produto",
            "itens.lote",
            "confirmadoPor"
    })
    Optional<Expedicao> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Expedicao e where e.id = :id")
    Optional<Expedicao> buscarParaAlteracao(@Param("id") Long id);
}