package com.portfolio.rastreabilidade.estoque;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository
        extends JpaRepository<MovimentacaoEstoque, Long> {

    @EntityGraph(attributePaths = {
            "produto",
            "lote",
            "registradoPor"
    })
    List<MovimentacaoEstoque>
            findByRecebimentoItemRecebimentoIdOrderByIdAsc(Long recebimentoId);
}