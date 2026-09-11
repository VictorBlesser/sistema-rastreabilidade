package com.portfolio.rastreabilidade.produto;

public enum UnidadeMedida {

    UN("Unidade"),
    CX("Caixa"),
    PCT("Pacote"),
    KG("Quilograma"),
    G("Grama"),
    L("Litro"),
    ML("Mililitro"),
    M("Metro");

    private final String rotulo;

    UnidadeMedida(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}