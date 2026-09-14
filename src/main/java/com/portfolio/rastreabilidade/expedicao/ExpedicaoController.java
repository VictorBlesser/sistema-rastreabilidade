package com.portfolio.rastreabilidade.expedicao;

import com.portfolio.rastreabilidade.lote.Lote;
import com.portfolio.rastreabilidade.lote.LoteService;
import com.portfolio.rastreabilidade.produto.Produto;
import com.portfolio.rastreabilidade.produto.ProdutoService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/expedicoes")
public class ExpedicaoController {

    private final ExpedicaoService service;
    private final ProdutoService produtoService;
    private final LoteService loteService;

    public ExpedicaoController(
            ExpedicaoService service,
            ProdutoService produtoService,
            LoteService loteService) {

        this.service = service;
        this.produtoService = produtoService;
        this.loteService = loteService;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("expedicoes", service.listar());
        return "expedicoes/lista";
    }

    @GetMapping("/novo")
    String novo(Model model) {
        model.addAttribute("expedicaoForm", new ExpedicaoForm());
        return "expedicoes/formulario";
    }

    @PostMapping
    String criar(
            @Valid @ModelAttribute("expedicaoForm") ExpedicaoForm form,
            BindingResult resultado,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return "expedicoes/formulario";
        }

        Long id;

        try {
            id = service.criar(
                    form.getDestinatario(),
                    form.getDocumento(),
                    form.getDataExpedicao());
        } catch (IllegalArgumentException erro) {
            resultado.reject("expedicao.criacao", erro.getMessage());
            return "expedicoes/formulario";
        }

        redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Rascunho criado. Adicione os itens antes de confirmar.");

        return "redirect:/expedicoes/" + id;
    }

    @GetMapping("/{id}")
    String detalhar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("itemForm", new ExpedicaoItemForm());
        return carregarDetalhe(id, model, redirectAttributes);
    }

    @PostMapping("/{id}/itens")
    String adicionarItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("itemForm") ExpedicaoItemForm form,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return carregarDetalhe(id, model, redirectAttributes);
        }

        try {
            service.adicionarItem(
                    id,
                    form.getProdutoId(),
                    form.getLoteId(),
                    form.getQuantidade());
        } catch (IllegalArgumentException erro) {
            resultado.reject("expedicao.item", erro.getMessage());
            return carregarDetalhe(id, model, redirectAttributes);
        }

        redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Item adicionado ao rascunho.");

        return "redirect:/expedicoes/" + id;
    }

    @PostMapping("/{id}/confirmar")
    String confirmar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            service.confirmar(id);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Expedição confirmada. Saídas de estoque registradas.");
        } catch (IllegalArgumentException erro) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    erro.getMessage());
        }

        return "redirect:/expedicoes/" + id;
    }

    private String carregarDetalhe(
            Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Expedicao expedicao = service.buscarPorId(id);
            model.addAttribute("expedicao", expedicao);

            if (expedicao.isRascunho()) {
                model.addAttribute(
                        "produtos",
                        produtoService.listar().stream()
                                .filter(Produto::isAtivo)
                                .toList());

                model.addAttribute(
                        "lotes",
                        loteService.listar().stream()
                                .filter(Lote::isAtivo)
                                .filter(lote -> lote.getProduto().isAtivo())
                                .filter(lote -> lote.getDataValidade() == null
                                        || !lote.getDataValidade().isBefore(
                                                expedicao.getDataExpedicao()))
                                .toList());
            }

            return "expedicoes/detalhe";
        } catch (IllegalArgumentException erro) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    erro.getMessage());

            return "redirect:/expedicoes";
        }
    }
}