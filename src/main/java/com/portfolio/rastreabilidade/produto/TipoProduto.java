package com.portfolio.rastreabilidade.produto;

public enum TipoProduto {
    MEDICAMENTO("Medicamento"),
    PRODUTO_MEDICO("Produto médico"),
    DIAGNOSTICO_IN_VITRO("Diagnóstico in vitro");

    private final String rotulo;

    TipoProduto(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
