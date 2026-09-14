package com.portfolio.rastreabilidade.recebimento;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public class RecebimentoItemForm {

    @NotNull(message = "Selecione um produto")
    private Long produtoId;

    private Long loteId;

    @NotNull(message = "Informe a quantidade")
    @DecimalMin(
            value = "0",
            inclusive = false,
            message = "A quantidade deve ser maior que zero")
    @Digits(
            integer = 13,
            fraction = 6,
            message = "Use até 13 dígitos inteiros e seis casas decimais")
    private BigDecimal quantidade;

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }
}