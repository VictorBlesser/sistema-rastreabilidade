package com.portfolio.rastreabilidade.expedicao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.portfolio.rastreabilidade.estoque.MovimentacaoEstoqueRepository;
import com.portfolio.rastreabilidade.estoque.TipoMovimentacao;
import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.produto.TipoProduto;
import com.portfolio.rastreabilidade.produto.UnidadeMedida;
import com.portfolio.rastreabilidade.recebimento.RecebimentoRepository;
import com.portfolio.rastreabilidade.recebimento.RecebimentoService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = "ADMINISTRADOR")
class ExpedicaoConcorrenciaIntegrationTest {

    @Autowired
    private ExpedicaoService expedicaoService;

    @Autowired
    private ExpedicaoRepository expedicaoRepository;

    @Autowired
    private RecebimentoService recebimentoService;

    @Autowired
    private RecebimentoRepository recebimentoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final List<Long> expedicaoIds = new ArrayList<>();

    private Long produtoId;
    private Long recebimentoId;

    @BeforeEach
    void preparar() {
        produtoId = produtoRepository.saveAndFlush(new Produto(
                "CON-" + UUID.randomUUID(),
                "Produto de teste de concorrência",
                null,
                TipoProduto.PRODUTO_MEDICO,
                false,
                false,
                false,
                UnidadeMedida.UN)).getId();

        recebimentoId = recebimentoService.criar(
                "Fornecedor de teste",
                "REC-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 14));

        recebimentoService.adicionarItem(
                recebimentoId,
                produtoId,
                null,
                new BigDecimal("5"));

        recebimentoService.confirmar(recebimentoId);

        criarExpedicao();
        criarExpedicao();
    }

    @AfterEach
    void limpar() {
        new TransactionTemplate(transactionManager).executeWithoutResult(
                status -> {
                    for (Long id : expedicaoIds) {
                        movimentacaoRepository.deleteAll(
                                movimentacaoRepository
                                        .findByExpedicaoItemExpedicaoIdOrderByIdAsc(id));
                    }

                    if (recebimentoId != null) {
                        movimentacaoRepository.deleteAll(
                                movimentacaoRepository
                                        .findByRecebimentoItemRecebimentoIdOrderByIdAsc(
                                                recebimentoId));
                    }

                    movimentacaoRepository.flush();

                    for (Long id : expedicaoIds) {
                        expedicaoRepository.deleteById(id);
                    }

                    expedicaoRepository.flush();

                    if (recebimentoId != null) {
                        recebimentoRepository.deleteById(recebimentoId);
                        recebimentoRepository.flush();
                    }

                    if (produtoId != null) {
                        produtoRepository.deleteById(produtoId);
                        produtoRepository.flush();
                    }
                });
    }

    @Test
    void deveConfirmarApenasUmaExpedicaoQuandoSaldoNaoAtendeAsDuas()
            throws Exception {

        CountDownLatch prontas = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> primeira = executor.submit(
                    () -> tentarConfirmar(
                            expedicaoIds.get(0), prontas, iniciar));

            Future<Boolean> segunda = executor.submit(
                    () -> tentarConfirmar(
                            expedicaoIds.get(1), prontas, iniciar));

            assertTrue(
                    prontas.await(10, TimeUnit.SECONDS),
                    "As duas tarefas devem estar prontas");

            iniciar.countDown();

            boolean primeiraConfirmada = primeira.get(20, TimeUnit.SECONDS);
            boolean segundaConfirmada = segunda.get(20, TimeUnit.SECONDS);

            int sucessos = (primeiraConfirmada ? 1 : 0)
                    + (segundaConfirmada ? 1 : 0);

            assertEquals(1, sucessos);

            Long confirmadaId = primeiraConfirmada
                    ? expedicaoIds.get(0)
                    : expedicaoIds.get(1);

            Long recusadaId = primeiraConfirmada
                    ? expedicaoIds.get(1)
                    : expedicaoIds.get(0);

            assertEquals(
                    StatusExpedicao.CONFIRMADO,
                    expedicaoService.buscarPorId(confirmadaId).getStatus());

            assertEquals(
                    StatusExpedicao.RASCUNHO,
                    expedicaoService.buscarPorId(recusadaId).getStatus());

            assertEquals(
                    1,
                    movimentacaoRepository
                            .findByExpedicaoItemExpedicaoIdOrderByIdAsc(confirmadaId)
                            .size());

            assertTrue(movimentacaoRepository
                    .findByExpedicaoItemExpedicaoIdOrderByIdAsc(recusadaId)
                    .isEmpty());

            BigDecimal saldo = movimentacaoRepository.consultarSaldo(
                    produtoId,
                    null,
                    TipoMovimentacao.ENTRADA);

            assertEquals(0, BigDecimal.ONE.compareTo(saldo));
        } finally {
            iniciar.countDown();
            executor.shutdownNow();

            assertTrue(
                    executor.awaitTermination(10, TimeUnit.SECONDS),
                    "As tarefas devem terminar antes da limpeza");
        }
    }

    private boolean tentarConfirmar(
            Long id,
            CountDownLatch prontas,
            CountDownLatch iniciar) throws InterruptedException {

        var contexto = SecurityContextHolder.createEmptyContext();

        contexto.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        AuthorityUtils.createAuthorityList(
                                "ROLE_ADMINISTRADOR")));

        SecurityContextHolder.setContext(contexto);

        try {
            prontas.countDown();

            if (!iniciar.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Tempo esgotado aguardando o início");
            }

            try {
                expedicaoService.confirmar(id);
                return true;
            } catch (IllegalArgumentException erro) {
                if (!erro.getMessage().contains("Saldo insuficiente")) {
                    throw erro;
                }

                return false;
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void criarExpedicao() {
        Long id = expedicaoService.criar(
                "Destinatário de teste",
                "EXP-" + UUID.randomUUID(),
                LocalDate.of(2026, 9, 14));

        expedicaoIds.add(id);

        expedicaoService.adicionarItem(
                id,
                produtoId,
                null,
                new BigDecimal("4"));
    }
}