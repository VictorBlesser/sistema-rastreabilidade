package com.portfolio.rastreabilidade.produto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ProdutoQuantidadeTest {

    @Test
    void deveManterPadraoDosConstrutoresExistentes() {
        Produto produto = new Produto(
                "PROD-001",
                "Produto",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true);

        assertFalse(produto.isFracionavel());
        assertEquals(UnidadeMedida.UN, produto.getUnidadeMedida());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "10", "10.000"})
    void deveAceitarQuantidadeInteira(String valor) {
        Produto produto = criarProduto(false);

        assertDoesNotThrow(
                () -> produto.validarQuantidade(new BigDecimal(valor)));
    }

    @Test
    void deveRejeitarFracaoParaProdutoNaoFracionavel() {
        Produto produto = criarProduto(false);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> produto.validarQuantidade(new BigDecimal("1.5")));

        assertEquals(
                "Este produto aceita somente quantidades inteiras",
                erro.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.001", "1.5", "2.75", "10"})
    void deveAceitarQuantidadeParaProdutoFracionavel(String valor) {
        Produto produto = criarProduto(true);

        assertDoesNotThrow(
                () -> produto.validarQuantidade(new BigDecimal(valor)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.5"})
    void deveRejeitarQuantidadeNaoPositiva(String valor) {
        Produto produto = criarProduto(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> produto.validarQuantidade(new BigDecimal(valor)));
    }

    @Test
    void deveRejeitarQuantidadeNula() {
        Produto produto = criarProduto(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> produto.validarQuantidade(null));
    }

    @Test
    void deveRejeitarUnidadeNula() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Produto(
                        "PROD-002",
                        "Produto",
                        null,
                        TipoProduto.PRODUTO_MEDICO,
                        true,
                        true,
                        false,
                        null));
    }

    private Produto criarProduto(boolean fracionavel) {
        return new Produto(
                "PROD-TESTE",
                "Produto de teste",
                null,
                TipoProduto.PRODUTO_MEDICO,
                true,
                true,
                fracionavel,
                UnidadeMedida.KG);
    }
}