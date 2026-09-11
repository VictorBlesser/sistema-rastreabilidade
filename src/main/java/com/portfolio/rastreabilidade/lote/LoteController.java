package com.portfolio.rastreabilidade.lote;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/lotes")
public class LoteController {

    private static final String FORMULARIO = "lotes/formulario";

    private final LoteService service;

    public LoteController(LoteService service) {
        this.service = service;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("lotes", service.listar());
        return "lotes/lista";
    }

    @GetMapping("/novo")
    String novo(Model model) {
        model.addAttribute("loteForm", new LoteForm());
        adicionarProdutos(model);
        return FORMULARIO;
    }

    @PostMapping
    String cadastrar(
            @Valid @ModelAttribute("loteForm") LoteForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            adicionarProdutos(model);
            return FORMULARIO;
        }

        try {
            service.cadastrar(
                    form.getProdutoId(),
                    form.getNumero(),
                    form.getDataFabricacao(),
                    form.getDataValidade());

        } catch (IllegalArgumentException erro) {
            bindingResult.reject("lote.cadastro", erro.getMessage());
            adicionarProdutos(model);
            return FORMULARIO;

        } catch (DataIntegrityViolationException erro) {
            bindingResult.reject(
                    "lote.integridade",
                    "Não foi possível cadastrar. Verifique se o lote já existe para o produto.");

            adicionarProdutos(model);
            return FORMULARIO;
        }

        redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Lote cadastrado com sucesso");

        return "redirect:/lotes";
    }

    @GetMapping("/{id}")
    String detalhar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("lote", service.buscarPorId(id));
            return "lotes/detalhe";

        } catch (IllegalArgumentException erro) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    erro.getMessage());

            return "redirect:/lotes";
        }
    }

    private void adicionarProdutos(Model model) {
        model.addAttribute("produtos", service.listarProdutosDisponiveis());
    }
}