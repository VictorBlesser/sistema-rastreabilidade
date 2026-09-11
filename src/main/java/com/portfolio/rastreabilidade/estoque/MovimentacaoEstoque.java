package com.portfolio.rastreabilidade.estoque;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.portfolio.rastreabilidade.lote.Lote;
import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.recebimento.Recebimento;
import com.portfolio.rastreabilidade.recebimento.RecebimentoItem;
import com.portfolio.rastreabilidade.recebimento.StatusRecebimento;
import com.portfolio.rastreabilidade.usuario.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "movimentacao_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "recebimento_item_id",
            nullable = false,
            unique = true,
            updatable = false)
    private RecebimentoItem recebimentoItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false, updatable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", updatable = false)
    private Lote lote;

    @Column(nullable = false, precision = 19, scale = 6, updatable = false)
    private BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private TipoMovimentacao tipo;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private OffsetDateTime registradoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por", nullable = false, updatable = false)
    private Usuario registradoPor;

    protected MovimentacaoEstoque() {
    }

    public MovimentacaoEstoque(RecebimentoItem item) {
        if (item == null || item.getId() == null) {
            throw new IllegalArgumentException(
                    "O item precisa estar salvo antes de gerar a movimentação");
        }

        Recebimento recebimento = item.getRecebimento();

        if (recebimento.getStatus() != StatusRecebimento.CONFIRMADO) {
            throw new IllegalArgumentException(
                    "O recebimento precisa estar confirmado");
        }

        if (recebimento.getConfirmadoPor() == null
                || recebimento.getConfirmadoPor().getId() == null
                || recebimento.getConfirmadoEm() == null) {

            throw new IllegalArgumentException(
                    "Os dados da confirmação são obrigatórios");
        }

        this.recebimentoItem = item;
        this.produto = item.getProduto();
        this.lote = item.getLote();
        this.quantidade = item.getQuantidade();
        this.tipo = TipoMovimentacao.ENTRADA;
        this.registradoEm = recebimento.getConfirmadoEm();
        this.registradoPor = recebimento.getConfirmadoPor();
    }

    public Long getId() {
        return id;
    }

    public RecebimentoItem getRecebimentoItem() {
        return recebimentoItem;
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

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public OffsetDateTime getRegistradoEm() {
        return registradoEm;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }
}