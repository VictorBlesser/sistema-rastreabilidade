package com.portfolio.rastreabilidade.rastreabilidade;

import com.portfolio.rastreabilidade.lote.LoteService;

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
            Model model) {

        model.addAttribute("lotes", loteService.listar());
        model.addAttribute("loteSelecionadoId", loteId);

        if (loteId != null) {
            try {
                RastreabilidadeService.Resumo resumo = service.consultar(loteId);

                model.addAttribute("lote", resumo.lote());
                model.addAttribute("movimentos", resumo.movimentos());
                model.addAttribute("entradas", resumo.entradas());
                model.addAttribute("saidas", resumo.saidas());
                model.addAttribute("saldo", resumo.saldo());
            } catch (IllegalArgumentException erro) {
                model.addAttribute("mensagemErro", erro.getMessage());
            }
        }

        return "rastreabilidade/consulta";
    }
}