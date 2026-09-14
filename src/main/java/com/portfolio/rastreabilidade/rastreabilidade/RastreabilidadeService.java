package com.portfolio.rastreabilidade.rastreabilidade;

import java.math.BigDecimal;
import java.util.List;

import com.portfolio.rastreabilidade.estoque.TipoMovimentacao;
import com.portfolio.rastreabilidade.lote.Lote;
import com.portfolio.rastreabilidade.lote.LoteService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RastreabilidadeService {

    private final RastreabilidadeRepository repository;
    private final LoteService loteService;

    public RastreabilidadeService(
            RastreabilidadeRepository repository,
            LoteService loteService) {

        this.repository = repository;
        this.loteService = loteService;
    }

    @Transactional(readOnly = true)
    public Resumo consultar(Long loteId) {
        Lote lote = loteService.buscarPorId(loteId);

        List<MovimentoRastreabilidade> movimentos =
                repository.consultarPorLote(loteId);

        BigDecimal entradas = BigDecimal.ZERO;
        BigDecimal saidas = BigDecimal.ZERO;

        for (MovimentoRastreabilidade movimento : movimentos) {
            if (movimento.getTipo() == TipoMovimentacao.ENTRADA) {
                entradas = entradas.add(movimento.getQuantidade());
            } else if (movimento.getTipo() == TipoMovimentacao.SAIDA) {
                saidas = saidas.add(movimento.getQuantidade());
            }
        }

        return new Resumo(
                lote,
                List.copyOf(movimentos),
                entradas,
                saidas,
                entradas.subtract(saidas));
    }

    public record Resumo(
            Lote lote,
            List<MovimentoRastreabilidade> movimentos,
            BigDecimal entradas,
            BigDecimal saidas,
            BigDecimal saldo) {
    }
}