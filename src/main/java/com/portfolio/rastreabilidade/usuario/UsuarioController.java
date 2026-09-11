package com.portfolio.rastreabilidade.usuario;

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
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final String FORMULARIO = "usuarios/formulario";

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @ModelAttribute("perfis")
    PerfilUsuario[] perfis() {
        return PerfilUsuario.values();
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("usuarios", service.listar());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    String novo(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        return FORMULARIO;
    }

    @PostMapping
    String cadastrar(
            @Valid @ModelAttribute("usuarioForm") UsuarioForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            form.setSenha(null);
            return FORMULARIO;
        }

        try {
            service.cadastrar(
                    form.getNome(),
                    form.getLogin(),
                    form.getSenha(),
                    form.getPerfil());
        } catch (IllegalArgumentException erro) {
            bindingResult.reject("usuario.cadastro", erro.getMessage());
            return FORMULARIO;
        } catch (DataIntegrityViolationException erro) {
            bindingResult.reject(
                    "usuario.integridade",
                    "Não foi possível cadastrar. Verifique se o login já está em uso.");
            return FORMULARIO;
        } finally {
            form.setSenha(null);
        }

        redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Usuário cadastrado com sucesso");

        return "redirect:/usuarios";
    }

    @GetMapping("/{id}")
    String detalhar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("usuario", service.buscarPorId(id));
            return "usuarios/detalhe";
        } catch (IllegalArgumentException erro) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    erro.getMessage());

            return "redirect:/usuarios";
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
                    "Usuário inativado com sucesso");

            return "redirect:/usuarios/" + id;
        } catch (IllegalArgumentException erro) {
            redirectAttributes.addFlashAttribute(
                    "mensagemErro",
                    erro.getMessage());

            return "redirect:/usuarios";
        }
    }
    @PostMapping("/{id}/ativar")
String ativar(
        @PathVariable Long id,
        RedirectAttributes redirectAttributes) {

    try {
        service.ativar(id);

        redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Usuário ativado com sucesso");

        return "redirect:/usuarios/" + id;
    } catch (IllegalArgumentException erro) {
        redirectAttributes.addFlashAttribute(
                "mensagemErro",
                erro.getMessage());

        return "redirect:/usuarios";
    }
}
}