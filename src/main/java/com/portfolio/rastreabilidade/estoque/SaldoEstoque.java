package com.portfolio.rastreabilidade.estoque;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.portfolio.rastreabilidade.produto.UnidadeMedida;

public interface SaldoEstoque {

    Long getProdutoId();

    String getCodigo();

    String getNome();

    UnidadeMedida getUnidadeMedida();

    Long getLoteId();

    String getNumeroLote();

    LocalDate getDataValidade();

    BigDecimal getQuantidade();
}