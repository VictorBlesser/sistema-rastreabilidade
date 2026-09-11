package com.portfolio.rastreabilidade.produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProdutoForm {

    @NotBlank(message = "o código é obrigatório")
    @Size(max = 50, message = "o código deve ter no máximo 50 caracteres")
    private String codigo;

    @NotBlank(message = "o nome é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    private String nome;

    @Size(max = 500, message = "a descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "o tipo é obrigatório")
    private TipoProduto tipo;

    private boolean controlaLote;
    private boolean controlaValidade;
    private boolean fracionavel;

    @NotNull(message = "A unidade de medida é obrigatória")
    private UnidadeMedida unidadeMedida = UnidadeMedida.UN;

    public ProdutoForm() {
    }

    public Produto toProduto() {
        return new Produto(
                codigo,
                nome,
                descricao,
                tipo,
                controlaLote,
                controlaValidade,
                fracionavel,
                unidadeMedida);
    }

    public UnidadeMedida[] getUnidadesDisponiveis() {
        return UnidadeMedida.values();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoProduto getTipo() {
        return tipo;
    }

    public void setTipo(TipoProduto tipo) {
        this.tipo = tipo;
    }

    public boolean isControlaLote() {
        return controlaLote;
    }

    public void setControlaLote(boolean controlaLote) {
        this.controlaLote = controlaLote;
    }

    public boolean isControlaValidade() {
        return controlaValidade;
    }

    public void setControlaValidade(boolean controlaValidade) {
        this.controlaValidade = controlaValidade;
    }

    public boolean isFracionavel() {
        return fracionavel;
    }

    public void setFracionavel(boolean fracionavel) {
        this.fracionavel = fracionavel;
    }

    public UnidadeMedida getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(UnidadeMedida unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }
}