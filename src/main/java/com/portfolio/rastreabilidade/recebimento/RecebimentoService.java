package com.portfolio.rastreabilidade.recebimento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoque;
import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoqueRepository;
import com.portfolio.rastreabilidade.lote.Lote;
import com.portfolio.rastreabilidade.lote.LoteRepository;
import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.usuario.Usuario;
import com.portfolio.rastreabilidade.usuario.UsuarioRepository;

import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecebimentoService {

    private final RecebimentoRepository repository;
    private final ProdutoRepository produtoRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    public RecebimentoService(
            RecebimentoRepository repository,
            ProdutoRepository produtoRepository,
            LoteRepository loteRepository,
            UsuarioRepository usuarioRepository,
            MovimentacaoEstoqueRepository movimentacaoRepository) {

        this.repository = repository;
        this.produtoRepository = produtoRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @Transactional
    public Long criar(
            String fornecedor,
            String documento,
            LocalDate dataRecebimento) {

        Recebimento recebimento = new Recebimento(
                fornecedor,
                documento,
                dataRecebimento);

        return repository.saveAndFlush(recebimento).getId();
    }

    @Transactional(readOnly = true)
    public List<Recebimento> listar() {
        return repository.findAll(
                Sort.by(Sort.Direction.DESC, "dataRecebimento", "id"));
    }

    @Transactional(readOnly = true)
    public Recebimento buscarPorId(Long id) {
        exigirId(id);

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Recebimento não encontrado"));
    }

    @Transactional
    public void adicionarItem(
            Long recebimentoId,
            Long produtoId,
            Long loteId,
            BigDecimal quantidade) {

        Recebimento recebimento = buscarParaAlteracao(recebimentoId);
        recebimento.exigirRascunho();

        if (produtoId == null) {
            throw new IllegalArgumentException("O produto é obrigatório");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Produto não encontrado"));

        Lote lote = null;

        if (loteId != null) {
            lote = loteRepository.findById(loteId)
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Lote não encontrado"));
        }

        RecebimentoItem item = new RecebimentoItem(
                recebimento,
                produto,
                lote,
                quantidade);

        recebimento.adicionarItem(item);
        repository.saveAndFlush(recebimento);
    }

    @Transactional
    public void confirmar(Long recebimentoId) {
        Recebimento recebimento = buscarParaAlteracao(recebimentoId);
        recebimento.exigirRascunho();

        Usuario usuario = buscarUsuarioAutenticado();

        recebimento.confirmar(usuario);

        repository.flush();

        List<MovimentacaoEstoque> movimentacoes = recebimento.getItens()
                .stream()
                .map(MovimentacaoEstoque::new)
                .toList();

        movimentacaoRepository.saveAllAndFlush(movimentacoes);
    }

    private Recebimento buscarParaAlteracao(Long id) {
        exigirId(id);

        return repository.buscarParaAlteracao(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Recebimento não encontrado"));
    }

    private Usuario buscarUsuarioAutenticado() {
        Authentication autenticacao = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (autenticacao == null
                || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {

            throw new IllegalArgumentException(
                    "Entre no sistema para confirmar o recebimento");
        }

        Usuario usuario = usuarioRepository
                .findByLogin(autenticacao.getName())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuário autenticado não encontrado"));

        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("O usuário está inativo");
        }

        return usuario;
    }

    private void exigirId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID é obrigatório");
        }
    }
}