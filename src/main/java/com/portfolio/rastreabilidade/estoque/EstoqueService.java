package com.portfolio.rastreabilidade.estoque;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstoqueService {

    private final MovimentacaoEstoqueRepository repository;

    public EstoqueService(MovimentacaoEstoqueRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SaldoEstoque> listarSaldos() {
        return repository.consultarSaldos(TipoMovimentacao.ENTRADA);
    }
}