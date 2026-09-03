package com.portfolio.rastreabilidade.produto;

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
    private boolean ativo;

    protected Produto() {
    }

    public Produto(
            String codigo,
            String nome,
            TipoProduto tipo,
            boolean controlaLote,
            boolean controlaValidade) {
        this(codigo, nome, null, tipo, controlaLote, controlaValidade);
    }

    public Produto(
            String codigo,
            String nome,
            String descricao,
            TipoProduto tipo,
            boolean controlaLote,
            boolean controlaValidade) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("O código é obrigatório");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome é obrigatório");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo é obrigatório");
        }

        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.nome = nome.trim();
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
        this.tipo = tipo;
        this.controlaLote = controlaLote;
        this.controlaValidade = controlaValidade;
        this.ativo = true;
    }

    public void inativar() {
        this.ativo = false;
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

    public boolean isAtivo() {
        return ativo;
    }

}
