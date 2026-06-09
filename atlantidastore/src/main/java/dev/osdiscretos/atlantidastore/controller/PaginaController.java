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

    @GetMapping("/perfil")
    public String perfil() {
        return "forward:/perfil.html";
    }

    @GetMapping("/biblioteca")
    public String biblioteca() {
        return "forward:/biblioteca.html";
    }

    @GetMapping("/publicar-jogo")
    public String publicarJogo() {
        return "forward:/publicar-jogo.html";
    }

    @GetMapping("/perfil-usuario")
    public String perfilUsuario() {
        return "forward:/perfil-usuario.html";
    }
}
