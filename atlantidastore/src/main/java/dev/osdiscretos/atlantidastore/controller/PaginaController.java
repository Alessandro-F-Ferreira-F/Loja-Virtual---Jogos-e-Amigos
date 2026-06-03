package dev.osdiscretos.atlantidastore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {
    @GetMapping({"/", "/usuarios"})
    public String usuarios() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "forward:/cadastro.html";
    }
}
