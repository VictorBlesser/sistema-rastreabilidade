package com.portfolio.rastreabilidade.expedicao;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.portfolio.rastreabilidade.lote.Lote;
import com.portfolio.rastreabilidade.produto.Produto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "expedicao_item")
public class ExpedicaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expedicao_id", nullable = false)
    private Expedicao expedicao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id")
    private Lote lote;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal quantidade;

    protected ExpedicaoItem() {
    }

    public ExpedicaoItem(
            Expedicao expedicao,
            Produto produto,
            Lote lote,
            BigDecimal quantidade) {

        if (expedicao == null) {
            throw new IllegalArgumentException(
                    "A expedição é obrigatória");
        }

        expedicao.exigirRascunho();

        this.expedicao = expedicao;
        this.produto = produto;
        this.lote = lote;
        this.quantidade = quantidade;

        validar();
    }

    public void validar() {
        if (produto == null) {
            throw new IllegalArgumentException(
                    "O produto é obrigatório");
        }

        if (!produto.isAtivo()) {
            throw new IllegalArgumentException(
                    "O produto está inativo");
        }

        produto.validarQuantidade(quantidade);

        BigDecimal quantidadeNormalizada;

        try {
            quantidadeNormalizada = quantidade.setScale(
                    6,
                    RoundingMode.UNNECESSARY);
        } catch (ArithmeticException erro) {
            throw new IllegalArgumentException(
                    "A quantidade deve ter no máximo seis casas decimais");
        }

        if (quantidadeNormalizada.precision() > 19) {
            throw new IllegalArgumentException(
                    "A quantidade excede o limite permitido");
        }

        if (produto.isControlaLote() && lote == null) {
            throw new IllegalArgumentException(
                    "O lote é obrigatório para este produto");
        }

        if (!produto.isControlaLote() && lote != null) {
            throw new IllegalArgumentException(
                    "Este produto não utiliza controle de lote");
        }

        if (lote != null) {
            if (!lote.isAtivo()) {
                throw new IllegalArgumentException(
                        "O lote está inativo");
            }

            Long produtoId = produto.getId();
            Long produtoDoLoteId = lote.getProduto().getId();

            if (produtoId == null || !produtoId.equals(produtoDoLoteId)) {
                throw new IllegalArgumentException(
                        "O lote não pertence ao produto informado");
            }

            if (produto.isControlaValidade()
                    && lote.getDataValidade() == null) {
                throw new IllegalArgumentException(
                        "O lote precisa ter validade informada");
            }

            if (lote.getDataValidade() != null
                    && lote.getDataValidade().isBefore(
                            expedicao.getDataExpedicao())) {
                throw new IllegalArgumentException(
                        "O lote está vencido na data da expedição");
            }
        }

        this.quantidade = quantidadeNormalizada;
    }

    public Long getId() {
        return id;
    }

    public Expedicao getExpedicao() {
        return expedicao;
    }

    public Produto getProduto() {
        return produto;
    }

    public Lote getLote() {
        return lote;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }
}