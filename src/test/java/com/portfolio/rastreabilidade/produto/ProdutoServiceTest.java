package com.portfolio.rastreabilidade.produto;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProdutoServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoService service;

    @Test
    void deveCadastrarProdutoQuandoCodigoNaoExiste() {
        Produto produto = new Produto(
                "CAT-001",
                "Cateter",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true
        );

        when(repository.existsByCodigoIgnoreCase("CAT-001")).thenReturn(false);
        when(repository.save(produto)).thenReturn(produto);

        Produto resultado = service.cadastrar(produto);
        assertSame(produto, resultado);
        verify(repository).save(produto);
    }

    @Test
    void deveRecusarCadastroQuandoCodigoJaExiste() {
        Produto produto = new Produto(
                "CAT-001",
                "Cateter",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true
        );

        when(repository.existsByCodigoIgnoreCase("CAT-001")).thenReturn(true);
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrar(produto)
        );

        assertEquals("Código já cadastrado", erro.getMessage());
        verify(repository, never()).save(produto);
    }

    @Test
    void deveListarProdutosOrdenadosPorNome() {
        Produto produto1 = new Produto(
                "CAT-001",
                "Cateter",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true
        );
        Produto produto2 = new Produto(
                "LUV-001",
                "Luva cirúrgica",
                TipoProduto.PRODUTO_MEDICO,
                true,
                true
        );
        List<Produto> produtos = List.of(produto1, produto2);
        Sort ordenacao = Sort.by("nome");

        when(repository.findAll(ordenacao)).thenReturn(produtos);

        List<Produto> resultado = service.listar();

        assertSame(produtos, resultado);
        verify(repository).findAll(ordenacao);
    }
}
