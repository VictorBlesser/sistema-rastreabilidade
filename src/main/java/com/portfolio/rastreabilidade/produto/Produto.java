package com.portfolio.rastreabilidade.produto;

import java.math.BigDecimal;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoProduto tipo;

    @Column(name = "controla_lote", nullable = false)
    private boolean controlaLote;

    @Column(name = "controla_validade", nullable = false)
    private boolean controlaValidade;

    @Column(nullable = false)
    private boolean fracionavel;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false, length = 10)
    private UnidadeMedida unidadeMedida;

    @Column(nullable = false)
    private boolean ativo;

    protected Produto() {
    }

    public Produto(
            String codigo,
            String nome,
            TipoProduto tipo,
            boolean controlaLote,
            boolean controlaValidade) {

        this(
                codigo,
                nome,
                null,
                tipo,
                controlaLote,
                controlaValidade,
                false,
                UnidadeMedida.UN);
    }

    public Produto(
            String codigo,
            String nome,
            String descricao,
            TipoProduto tipo,
            boolean controlaLote,
            boolean controlaValidade) {

        this(
                codigo,
                nome,
                descricao,
                tipo,
                controlaLote,
                controlaValidade,
                false,
                UnidadeMedida.UN);
    }

    public Produto(
            String codigo,
            String nome,
            String descricao,
            TipoProduto tipo,
            boolean controlaLote,
            boolean controlaValidade,
            boolean fracionavel,
            UnidadeMedida unidadeMedida) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código é obrigatório");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("O tipo é obrigatório");
        }

        if (unidadeMedida == null) {
            throw new IllegalArgumentException("A unidade de medida é obrigatória");
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nome = nome.trim();
        this.descricao = descricao == null || descricao.isBlank()
                ? null
                : descricao.trim();
        this.tipo = tipo;
        this.controlaLote = controlaLote;
        this.controlaValidade = controlaValidade;
        this.fracionavel = fracionavel;
        this.unidadeMedida = unidadeMedida;
        this.ativo = true;
    }

    public void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade deve ser maior que zero");
        }

        if (!fracionavel && quantidade.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException(
                    "Este produto aceita somente quantidades inteiras");
        }
    }

    public void inativar() {
        this.ativo = false;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoProduto getTipo() {
        return tipo;
    }

    public boolean isControlaLote() {
        return controlaLote;
    }

    public boolean isControlaValidade() {
        return controlaValidade;
    }

    public boolean isFracionavel() {
        return fracionavel;
    }

    public UnidadeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public boolean isAtivo() {
        return ativo;
    }
}