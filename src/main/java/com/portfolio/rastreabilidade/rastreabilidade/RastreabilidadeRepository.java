package com.portfolio.rastreabilidade.rastreabilidade;

import java.util.List;

import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoque;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface RastreabilidadeRepository
        extends Repository<MovimentacaoEstoque, Long> {

    @Query("""
            select
                m.id as id,
                m.tipo as tipo,
                m.quantidade as quantidade,
                m.registradoEm as registradoEm,
                u.nome as responsavel,
                coalesce(r.fornecedor, e.destinatario) as participante,
                coalesce(r.documento, e.documento) as documento,
                r.id as recebimentoId,
                e.id as expedicaoId
            from MovimentacaoEstoque m
            join m.registradoPor u
            left join m.recebimentoItem ri
            left join ri.recebimento r
            left join m.expedicaoItem ei
            left join ei.expedicao e
            where m.lote.id = :loteId
            order by m.registradoEm, m.id
            """)
    List<MovimentoRastreabilidade> consultarPorLote(
            @Param("loteId") Long loteId);
}