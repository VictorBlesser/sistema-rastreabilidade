package com.portfolio.rastreabilidade.rastreabilidade;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.portfolio.rastreabilidade.estoque.TipoMovimentacao;
import com.portfolio.rastreabilidade.lote.LoteService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RastreabilidadeController {

    private final RastreabilidadeService service;
    private final LoteService loteService;

    public RastreabilidadeController(
            RastreabilidadeService service,
            LoteService loteService) {

        this.service = service;
        this.loteService = loteService;
    }

    @GetMapping("/rastreabilidade")
    String consultar(
            @RequestParam(required = false) Long loteId,
            Authentication authentication,
            Model model) {

        boolean podeVisualizarQuantidades = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals(
                                "ESTOQUE_QUANTIDADE_FISICA_VISUALIZAR"));

        model.addAttribute(
                "podeVisualizarQuantidades", podeVisualizarQuantidades);

        model.addAttribute("lotes", loteService.listar());
        model.addAttribute("loteSelecionadoId", loteId);

        if (loteId != null) {
            try {
                RastreabilidadeService.Resumo resumo = service.consultar(loteId);

                model.addAttribute("lote", resumo.lote());

                model.addAttribute(
                        "movimentos",
                        resumo.movimentos().stream()
                                .map(movimento -> new MovimentoTela(
                                        movimento.getTipo(),
                                        podeVisualizarQuantidades
                                                ? movimento.getQuantidade()
                                                : null,
                                        movimento.getRegistradoEm(),
                                        movimento.getResponsavel(),
                                        movimento.getParticipante(),
                                        movimento.getDocumento(),
                                        movimento.getRecebimentoId(),
                                        movimento.getExpedicaoId()))
                                .toList());

                if (podeVisualizarQuantidades) {
                    model.addAttribute("entradas", resumo.entradas());
                    model.addAttribute("saidas", resumo.saidas());
                    model.addAttribute("saldo", resumo.saldo());
                }
            } catch (IllegalArgumentException erro) {
                model.addAttribute("mensagemErro", erro.getMessage());
            }
        }

        return "rastreabilidade/consulta";
    }

    public record MovimentoTela(
            TipoMovimentacao tipo,
            BigDecimal quantidade,
            OffsetDateTime registradoEm,
            String responsavel,
            String participante,
            String documento,
            Long recebimentoId,
            Long expedicaoId) {
    }
}