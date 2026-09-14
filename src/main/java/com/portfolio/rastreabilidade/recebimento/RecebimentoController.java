package com.portfolio.rastreabilidade.recebimento;

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
@RequestMapping("/recebimentos")
public class RecebimentoController {

    private final RecebimentoService service;
    private final ProdutoService produtoService;
    private final LoteService loteService;

    public RecebimentoController(
            RecebimentoService service,
            ProdutoService produtoService,
            LoteService loteService) {

        this.service = service;
        this.produtoService = produtoService;
        this.loteService = loteService;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("recebimentos", service.listar());
        return "recebimentos/lista";
    }

    @GetMapping("/novo")
    String novo(Model model) {
        model.addAttribute("recebimentoForm", new RecebimentoForm());
        return "recebimentos/formulario";
    }

    @PostMapping
    String criar(
            @Valid @ModelAttribute("recebimentoForm") RecebimentoForm form,
            BindingResult resultado,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return "recebimentos/formulario";
        }

        Long id;

        try {
            id = service.criar(
                    form.getFornecedor(),
                    form.getDocumento(),
                    form.getDataRecebimento());
        } catch (IllegalArgumentException erro) {
            resultado.reject("recebimento.criacao", erro.getMessage());
            return "recebimentos/formulario";
        }

        redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Rascunho criado. Adicione os itens antes de confirmar.");

        return "redirect:/recebimentos/" + id;
    }

    @GetMapping("/{id}")
    String detalhar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("itemForm", new RecebimentoItemForm());
        return carregarDetalhe(id, model, redirectAttributes);
    }

    @PostMapping("/{id}/itens")
    String adicionarItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("itemForm") RecebimentoItemForm form,
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
            resultado.reject("recebimento.item", erro.getMessage());
            return carregarDetalhe(id, model, redirectAttributes);
        }

        redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Item adicionado ao rascunho.");

        return "redirect:/recebimentos/" + id;
    }

    @PostMapping("/{id}/confirmar")
    String confirmar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            service.confirmar(id);
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Recebimento confirmado. Entradas de estoque registradas.");
        } catch (IllegalArgumentException erro) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    erro.getMessage());
        }

        return "redirect:/recebimentos/" + id;
    }

    private String carregarDetalhe(
            Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Recebimento recebimento = service.buscarPorId(id);
            model.addAttribute("recebimento", recebimento);

            if (recebimento.isRascunho()) {
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
                                .toList());
            }

            return "recebimentos/detalhe";
        } catch (IllegalArgumentException erro) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    erro.getMessage());

            return "redirect:/recebimentos";
        }
    }
}