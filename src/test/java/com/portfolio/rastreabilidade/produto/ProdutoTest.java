package com.portfolio.rastreabilidade.produto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoTest {

    @Test
    void deveCriarProdutoAtivo() {
        Produto produto = new Produto(
                "CAT-001",
                "Cateter",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true);
        assertTrue(produto.isAtivo());
    }

    @Test
    void deveInativarProduto() {
        Produto produto = new Produto(
                "CAT-001",
                "Cateter",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true);
        produto.inativar();
        assertFalse(produto.isAtivo());
    }

    @Test
    void deveNormalizarCodigoDoProduto() {
        Produto produto = new Produto(
                " cat-001 ",
                "Cateter",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true);
        assertEquals("CAT-001", produto.getCodigo());
    }

    @Test
    void deveNormalizarNomeEDescricaoDoProduto() {
        Produto produto = new Produto(
                "CAT-001",
                " Cateter ",
                " Uso hospitalar ",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true);

        assertEquals("Cateter", produto.getNome());
        assertEquals("Uso hospitalar", produto.getDescricao());
    }

    @Test
    void deveRecusarCodigoEmBranco() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Produto(
                        " ",
                        "Cateter",
                        TipoProduto.PRODUTO_MEDICO,
                        true,
                        true));
        assertEquals("O código é obrigatório", erro.getMessage());
    }

    @Test
    void deveRecusarNomeEmBranco() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Produto(
                        "CAT-001",
                        " ",
                        TipoProduto.PRODUTO_MEDICO,
                        true,
                        true));
        assertEquals("O nome é obrigatório", erro.getMessage());
    }

    @Test
    void deveRecusarTipoNulo() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new Produto(
                        "CAT-001",
                        "Cateter",
                        null,
                        true,
                        true));
        assertEquals("O tipo é obrigatório", erro.getMessage());
    }
}
