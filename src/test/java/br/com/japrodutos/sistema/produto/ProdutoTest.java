package br.com.japrodutos.sistema.produto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoTest {

    @Test // Teste para verificar se o produto é criado como ativo
    void deveCriarProdutoAtivo() {
        Produto produto = new Produto(
                "CAT-001",
                "Cateter",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true
        );
        assertTrue(produto.isAtivo());
    }
    @Test // Teste para verificar se o produto pode ser inativado
    void deveInativarProduto() {
        Produto produto = new Produto(
                "CAT-001",
                "Cateter",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true
        );
        produto.inativar();
        assertFalse(produto.isAtivo());
    }
}
