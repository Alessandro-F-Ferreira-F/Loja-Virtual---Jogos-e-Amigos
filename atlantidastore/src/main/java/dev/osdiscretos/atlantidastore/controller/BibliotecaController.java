package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.auth.SessionKey;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.JogoResumoDTO;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.service.AuthService;
import dev.osdiscretos.atlantidastore.service.BibliotecaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {
    private final BibliotecaService bibliotecaService;
    private final AuthService authService;

    public BibliotecaController(BibliotecaService bibliotecaService, AuthService authService) {
        this.bibliotecaService = bibliotecaService;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<JogoResumoDTO>> listarMinhaBiblioteca(
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        Usuario usuario = usuarioAutenticado(token);
        return ResponseEntity.ok(bibliotecaService.listarBiblioteca(usuario.getId()));
    }

    @PostMapping("/{jogoId}")
    public ResponseEntity<Void> adicionarJogo(
        @PathVariable UUID jogoId,
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        Usuario usuario = usuarioAutenticado(token);
        bibliotecaService.adicionarJogo(usuario.getId(), jogoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{jogoId}")
    public ResponseEntity<Void> removerJogo(
        @PathVariable UUID jogoId,
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        Usuario usuario = usuarioAutenticado(token);
        bibliotecaService.removerJogo(usuario.getId(), jogoId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(IllegalArgumentException exception) {
        return ResponseEntity
            .badRequest()
            .body(new ErroResponse(exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroResponse> tratarItemNaoEncontrado(NoSuchElementException exception) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErroResponse(exception.getMessage()));
    }

    private Usuario usuarioAutenticado(String token) {
        Usuario usuario = authService.findUserBySessionToken(token);

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login obrigatório");
        }

        return usuario;
    }
}
