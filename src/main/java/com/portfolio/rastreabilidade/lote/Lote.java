package com.portfolio.rastreabilidade.lote;

import java.time.LocalDate;
import java.util.Locale;

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
@Table(name = "lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, length = 100)
    private String numero;

    @Column(name = "data_fabricacao")
    private LocalDate dataFabricacao;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(nullable = false)
    private boolean ativo;

    protected Lote() {
    }

    public Lote(
            Produto produto,
            String numero,
            LocalDate dataFabricacao,
            LocalDate dataValidade) {

        if (produto == null) {
            throw new IllegalArgumentException("O produto é obrigatório");
        }

        if (!produto.isAtivo()) {
            throw new IllegalArgumentException("O produto está inativo");
        }

        if (!produto.isControlaLote()) {
            throw new IllegalArgumentException("O produto não controla lote");
        }

        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("O número do lote é obrigatório");
        }

        String numeroNormalizado = numero.strip().toUpperCase(Locale.ROOT);

        if (numeroNormalizado.length() > 100) {
            throw new IllegalArgumentException(
                    "O número do lote deve ter no máximo 100 caracteres");
        }

        if (produto.isControlaValidade() && dataValidade == null) {
            throw new IllegalArgumentException(
                    "A validade é obrigatória para este produto");
        }

        if (dataFabricacao != null
                && dataValidade != null
                && dataValidade.isBefore(dataFabricacao)) {

            throw new IllegalArgumentException(
                    "A validade não pode ser anterior à fabricação");
        }

        this.produto = produto;
        this.numero = numeroNormalizado;
        this.dataFabricacao = dataFabricacao;
        this.dataValidade = dataValidade;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public String getNumero() {
        return numero;
    }

    public LocalDate getDataFabricacao() {
        return dataFabricacao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public boolean isAtivo() {
        return ativo;
    }
}