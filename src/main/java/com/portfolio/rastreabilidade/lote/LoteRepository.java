package com.portfolio.rastreabilidade.lote;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    boolean existsByProdutoIdAndNumero(Long produtoId, String numero);

    @Override
    @EntityGraph(attributePaths = "produto")
    List<Lote> findAll(Sort sort);

    @Override
    @EntityGraph(attributePaths = "produto")
    Optional<Lote> findById(Long id);
}