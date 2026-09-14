package com.portfolio.rastreabilidade.estoque;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimentacaoEstoqueRepository
        extends JpaRepository<MovimentacaoEstoque, Long> {

    @EntityGraph(attributePaths = {
            "produto",
            "lote",
            "registradoPor"
    })
    List<MovimentacaoEstoque>
            findByRecebimentoItemRecebimentoIdOrderByIdAsc(Long recebimentoId);

    @EntityGraph(attributePaths = {
            "produto",
            "lote",
            "registradoPor"
    })
    List<MovimentacaoEstoque>
            findByExpedicaoItemExpedicaoIdOrderByIdAsc(Long expedicaoId);

    @Query("""
            select
                p.id as produtoId,
                p.codigo as codigo,
                p.nome as nome,
                p.unidadeMedida as unidadeMedida,
                l.id as loteId,
                l.numero as numeroLote,
                l.dataValidade as dataValidade,
                sum(
                    case
                        when m.tipo = :entrada then m.quantidade
                        else -m.quantidade
                    end
                ) as quantidade
            from MovimentacaoEstoque m
            join m.produto p
            left join m.lote l
            group by
                p.id,
                p.codigo,
                p.nome,
                p.unidadeMedida,
                l.id,
                l.numero,
                l.dataValidade
            order by p.nome, p.codigo, l.numero
            """)
    List<SaldoEstoque> consultarSaldos(
            @Param("entrada") TipoMovimentacao entrada);

    @Query("""
            select coalesce(
                sum(
                    case
                        when m.tipo = :entrada then m.quantidade
                        else -m.quantidade
                    end
                ),
                0
            )
            from MovimentacaoEstoque m
            left join m.lote l
            where m.produto.id = :produtoId
              and (
                  (:loteId is null and l.id is null)
                  or l.id = :loteId
              )
            """)
    BigDecimal consultarSaldo(
            @Param("produtoId") Long produtoId,
            @Param("loteId") Long loteId,
            @Param("entrada") TipoMovimentacao entrada);
}