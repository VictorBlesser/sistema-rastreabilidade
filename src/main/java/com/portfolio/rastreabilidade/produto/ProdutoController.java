package com.portfolio.rastreabilidade.produto;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    private static final String VIEW_FORMULARIO = "produtos/formulario";

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("produtos", service.listar());
        return "produtos/lista";
    }

    @GetMapping("/novo")
    String novo(Model model) {
        model.addAttribute("produtoForm", new ProdutoForm());
        adicionarTiposProduto(model);
        return VIEW_FORMULARIO;
    }

    @PostMapping
    String cadastrar(
            @Valid @ModelAttribute("produtoForm") ProdutoForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            adicionarTiposProduto(model);
            return VIEW_FORMULARIO;
        }

        try {
            service.cadastrar(form.toProduto());
        } catch (IllegalArgumentException erro) {
            bindingResult.rejectValue("codigo", "produto.codigo.duplicado", erro.getMessage());
            adicionarTiposProduto(model);
            return VIEW_FORMULARIO;
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto cadastrado com sucesso");
        return "redirect:/produtos";
    }

    @GetMapping("/{id}")
    String detalhar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("produto", service.buscarPorId(id));
            return "produtos/detalhe";
        } catch (IllegalArgumentException erro) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    erro.getMessage());
            return "redirect:/produtos";
        }
    }

    @PostMapping("/{id}/inativar")
    String inativar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            service.inativar(id);

            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Produto inativado com sucesso");

            return "redirect:/produtos/" + id;
        } catch (IllegalArgumentException erro) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    erro.getMessage());

            return "redirect:/produtos";
        }
    }

    private void adicionarTiposProduto(Model model) {
        model.addAttribute("tiposProduto", TipoProduto.values());
    }

}
