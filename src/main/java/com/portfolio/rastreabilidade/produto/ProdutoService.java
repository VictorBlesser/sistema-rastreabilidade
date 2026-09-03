package com.portfolio.rastreabilidade.produto;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Produto cadastrar(Produto produto) {
        if (repository.existsByCodigoIgnoreCase(produto.getCodigo())) {
            throw new IllegalArgumentException("Código já cadastrado");
        }
        return repository.save(produto);
    }

    @Transactional(readOnly = true)
    public List<Produto> listar() {
        return repository.findAll(Sort.by("nome"));
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
    }

    @Transactional
    public Produto inativar(Long id) {
        Produto produto = buscarPorId(id);
        produto.inativar();

        return repository.save(produto);
    }
}
