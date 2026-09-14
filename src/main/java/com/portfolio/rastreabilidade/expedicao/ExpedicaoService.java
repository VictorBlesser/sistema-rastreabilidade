package com.portfolio.rastreabilidade.expedicao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoque;
import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoqueRepository;
import com.portfolio.rastreabilidade.estoque.TipoMovimentacao;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpedicaoService {

    private final ExpedicaoRepository repository;
    private final ProdutoRepository produtoRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    public ExpedicaoService(
            ExpedicaoRepository repository,
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
            String destinatario,
            String documento,
            LocalDate dataExpedicao) {

        Expedicao expedicao = new Expedicao(
                destinatario,
                documento,
                dataExpedicao);

        return repository.saveAndFlush(expedicao).getId();
    }

    @Transactional(readOnly = true)
    public List<Expedicao> listar() {
        return repository.findAll(
                Sort.by(Sort.Direction.DESC, "dataExpedicao", "id"));
    }

    @Transactional(readOnly = true)
    public Expedicao buscarPorId(Long id) {
        exigirId(id);

        return repository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Expedição não encontrada"));
    }

    @Transactional
    public void adicionarItem(
            Long expedicaoId,
            Long produtoId,
            Long loteId,
            BigDecimal quantidade) {

        Expedicao expedicao = buscarParaAlteracao(expedicaoId);
        expedicao.exigirRascunho();

        if (produtoId == null) {
            throw new IllegalArgumentException(
                    "O produto é obrigatório");
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

        ExpedicaoItem item = new ExpedicaoItem(
                expedicao,
                produto,
                lote,
                quantidade);

        expedicao.adicionarItem(item);
        repository.saveAndFlush(expedicao);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void confirmar(Long expedicaoId) {
        Expedicao expedicao = buscarParaAlteracao(expedicaoId);
        expedicao.exigirRascunho();

        Usuario usuario = buscarUsuarioAutenticado();

        if (expedicao.getItens().isEmpty()) {
            throw new IllegalArgumentException(
                    "Adicione pelo menos um item antes de confirmar");
        }

        bloquearProdutos(expedicao);
        validarSaldos(expedicao);

        expedicao.confirmar(usuario);
        repository.flush();

        List<MovimentacaoEstoque> movimentacoes = expedicao.getItens()
                .stream()
                .map(MovimentacaoEstoque::saida)
                .toList();

        movimentacaoRepository.saveAllAndFlush(movimentacoes);
    }

    private void bloquearProdutos(Expedicao expedicao) {
        List<Long> produtoIds = expedicao.getItens()
                .stream()
                .map(item -> item.getProduto().getId())
                .distinct()
                .sorted()
                .toList();

        for (Long produtoId : produtoIds) {
            produtoRepository.buscarParaMovimentacao(produtoId)
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Produto não encontrado"));
        }
    }

    private void validarSaldos(Expedicao expedicao) {
        Map<ChaveEstoque, BigDecimal> quantidades = new LinkedHashMap<>();

        for (ExpedicaoItem item : expedicao.getItens()) {
            item.validar();

            Long loteId = item.getLote() == null
                    ? null
                    : item.getLote().getId();

            ChaveEstoque chave = new ChaveEstoque(
                    item.getProduto().getId(),
                    loteId);

            quantidades.merge(
                    chave,
                    item.getQuantidade(),
                    BigDecimal::add);
        }

        for (Map.Entry<ChaveEstoque, BigDecimal> entrada
                : quantidades.entrySet()) {

            ChaveEstoque chave = entrada.getKey();
            BigDecimal solicitada = entrada.getValue();

            BigDecimal saldo = movimentacaoRepository.consultarSaldo(
                    chave.produtoId(),
                    chave.loteId(),
                    TipoMovimentacao.ENTRADA);

            if (solicitada.compareTo(saldo) > 0) {
                Produto produto = produtoRepository.findById(chave.produtoId())
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Produto não encontrado"));

                String identificacaoLote = chave.loteId() == null
                        ? "sem lote"
                        : "lote ID " + chave.loteId();

                throw new IllegalArgumentException(
                        "Saldo insuficiente para "
                                + produto.getNome()
                                + " (" + identificacaoLote + "). Disponível: "
                                + saldo.stripTrailingZeros().toPlainString()
                                + "; solicitado: "
                                + solicitada.stripTrailingZeros().toPlainString());
            }
        }
    }

    private Expedicao buscarParaAlteracao(Long id) {
        exigirId(id);

        return repository.buscarParaAlteracao(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Expedição não encontrada"));
    }

    private Usuario buscarUsuarioAutenticado() {
        Authentication autenticacao = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (autenticacao == null
                || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {

            throw new IllegalArgumentException(
                    "Entre no sistema para confirmar a expedição");
        }

        Usuario usuario = usuarioRepository
                .findByLogin(autenticacao.getName())
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Usuário autenticado não encontrado"));

        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException(
                    "O usuário está inativo");
        }

        return usuario;
    }

    private void exigirId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID é obrigatório");
        }
    }

    private record ChaveEstoque(Long produtoId, Long loteId) {
    }
}