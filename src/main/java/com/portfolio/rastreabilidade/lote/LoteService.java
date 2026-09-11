package com.portfolio.rastreabilidade.lote;

import java.time.LocalDate;
import java.util.List;

import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoteService {

    private final LoteRepository repository;
    private final ProdutoRepository produtoRepository;

    public LoteService(
            LoteRepository repository,
            ProdutoRepository produtoRepository) {

        this.repository = repository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Lote cadastrar(
            Long produtoId,
            String numero,
            LocalDate dataFabricacao,
            LocalDate dataValidade) {

        if (produtoId == null) {
            throw new IllegalArgumentException("O produto é obrigatório");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Produto não encontrado"));

        Lote lote = new Lote(
                produto,
                numero,
                dataFabricacao,
                dataValidade);

        if (repository.existsByProdutoIdAndNumero(produtoId, lote.getNumero())) {
            throw new IllegalArgumentException(
                    "Este lote já está cadastrado para o produto");
        }

        return repository.saveAndFlush(lote);
    }

    @Transactional(readOnly = true)
    public List<Lote> listar() {
        return repository.findAll(
                Sort.by("produto.nome", "numero", "id"));
    }

    @Transactional(readOnly = true)
    public Lote buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID é obrigatório");
        }

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException("Lote não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Produto> listarProdutosDisponiveis() {
        return produtoRepository.findAll(Sort.by("nome", "id"))
                .stream()
                .filter(Produto::isAtivo)
                .filter(Produto::isControlaLote)
                .toList();
    }
}