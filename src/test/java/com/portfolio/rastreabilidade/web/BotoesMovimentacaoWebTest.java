package com.portfolio.rastreabilidade.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.portfolio.rastreabilidade.expedicao.ExpedicaoService;
import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoRepository;
import com.portfolio.rastreabilidade.produto.TipoProduto;
import com.portfolio.rastreabilidade.recebimento.RecebimentoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BotoesMovimentacaoWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private RecebimentoService recebimentoService;

    @Autowired
    private ExpedicaoService expedicaoService;

    private Long recebimentoId;
    private Long expedicaoId;

    @BeforeEach
    void preparar() {
        Produto produto = produtoRepository.saveAndFlush(new Produto(
                "MOV-UI-" + UUID.randomUUID(),
                "Produto para teste de botões de movimentação",
                TipoProduto.PRODUTO_MEDICO,
                false,
                false));

        recebimentoId = recebimentoService.criar(
                "Fornecedor de teste",
                "REC-UI",
                LocalDate.now());

        recebimentoService.adicionarItem(
                recebimentoId,
                produto.getId(),
                null,
                BigDecimal.ONE);

        expedicaoId = expedicaoService.criar(
                "Destinatário de teste",
                "EXP-UI",
                LocalDate.now());

        expedicaoService.adicionarItem(
                expedicaoId,
                produto.getId(),
                null,
                BigDecimal.ONE);
    }

    @Test
    @WithMockUser(authorities = {
            "RECEBIMENTO_VISUALIZAR",
            "EXPEDICAO_VISUALIZAR"
    })
    void consultaNaoDeveExibirCriacaoEdicaoOuConfirmacao()
            throws Exception {

        verificarCriacao("/recebimentos", false);
        verificarCriacao("/expedicoes", false);

        verificarFormularios(
                "/recebimentos", recebimentoId, false, false);

        verificarFormularios(
                "/expedicoes", expedicaoId, false, false);
    }

    @Test
    @WithMockUser(authorities = {
            "RECEBIMENTO_VISUALIZAR",
            "RECEBIMENTO_CRIAR",
            "EXPEDICAO_VISUALIZAR",
            "EXPEDICAO_CRIAR"
    })
    void criacaoNaoDeveLiberarEdicaoOuConfirmacao()
            throws Exception {

        verificarCriacao("/recebimentos", true);
        verificarCriacao("/expedicoes", true);

        verificarFormularios(
                "/recebimentos", recebimentoId, false, false);

        verificarFormularios(
                "/expedicoes", expedicaoId, false, false);
    }

    @Test
    @WithMockUser(authorities = {
            "RECEBIMENTO_VISUALIZAR",
            "RECEBIMENTO_EDITAR",
            "EXPEDICAO_VISUALIZAR",
            "EXPEDICAO_EDITAR"
    })
    void edicaoNaoDeveExibirConfirmacao()
            throws Exception {

        verificarFormularios(
                "/recebimentos", recebimentoId, true, false);

        verificarFormularios(
                "/expedicoes", expedicaoId, true, false);
    }

    @Test
    @WithMockUser(authorities = {
            "RECEBIMENTO_VISUALIZAR",
            "RECEBIMENTO_CONFIRMAR",
            "EXPEDICAO_VISUALIZAR",
            "EXPEDICAO_CONFIRMAR"
    })
    void confirmacaoNaoDeveExibirEdicao()
            throws Exception {

        verificarFormularios(
                "/recebimentos", recebimentoId, false, true);

        verificarFormularios(
                "/expedicoes", expedicaoId, false, true);
    }

    @Test
    @WithMockUser(authorities = {
            "RECEBIMENTO_VISUALIZAR",
            "RECEBIMENTO_EDITAR",
            "RECEBIMENTO_CONFIRMAR",
            "EXPEDICAO_VISUALIZAR",
            "EXPEDICAO_EDITAR",
            "EXPEDICAO_CONFIRMAR"
    })
    void deveExibirEdicaoEConfirmacaoQuandoAutorizadas()
            throws Exception {

        verificarFormularios(
                "/recebimentos", recebimentoId, true, true);

        verificarFormularios(
                "/expedicoes", expedicaoId, true, true);
    }

    private void verificarCriacao(
            String modulo,
            boolean permitido) throws Exception {

        String link = "href=\"" + modulo + "/novo\"";

        mockMvc.perform(get(modulo))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        permitido
                                ? containsString(link)
                                : not(containsString(link))));
    }

    private void verificarFormularios(
            String modulo,
            Long id,
            boolean podeEditar,
            boolean podeConfirmar) throws Exception {

        String endereco = modulo + "/" + id;
        String editar = "action=\"" + endereco + "/itens\"";
        String confirmar = "action=\"" + endereco + "/confirmar\"";

        mockMvc.perform(get(endereco))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        podeEditar
                                ? containsString(editar)
                                : not(containsString(editar))))
                .andExpect(content().string(
                        podeConfirmar
                                ? containsString(confirmar)
                                : not(containsString(confirmar))));
    }
}