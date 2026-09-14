package com.portfolio.rastreabilidade.estoque;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    private final EstoqueService service;

    public EstoqueController(EstoqueService service) {
        this.service = service;
    }

    @GetMapping
    String listar(Model model) {
        model.addAttribute("saldos", service.listarSaldos());
        return "estoque/lista";
    }
}