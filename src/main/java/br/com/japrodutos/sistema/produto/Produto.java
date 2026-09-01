package br.com.japrodutos.sistema.produto;

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

        this.codigo = codigo;
        this.nome = nome;
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

    public TipoProduto getTipo() {
        return tipo;
    }

    public boolean isAtivo() {
        return ativo;
    }

}
