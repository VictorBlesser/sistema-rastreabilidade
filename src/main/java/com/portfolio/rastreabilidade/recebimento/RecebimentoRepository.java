package com.portfolio.rastreabilidade.recebimento;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecebimentoRepository
        extends JpaRepository<Recebimento, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "itens",
            "itens.produto",
            "itens.lote",
            "confirmadoPor"
    })
    Optional<Recebimento> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Recebimento r where r.id = :id")
    Optional<Recebimento> buscarParaAlteracao(@Param("id") Long id);
}