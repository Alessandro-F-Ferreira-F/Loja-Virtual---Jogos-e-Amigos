package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.auth.SessionKey;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.JogoResumoDTO;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.service.AuthService;
import dev.osdiscretos.atlantidastore.service.ListaDesejosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/lista-desejos")
public class ListaDesejosController {
    private final ListaDesejosService listaDesejosService;
    private final AuthService authService;

    public ListaDesejosController(ListaDesejosService listaDesejosService, AuthService authService) {
        this.listaDesejosService = listaDesejosService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<JogoResumoDTO>> listarMeusDesejos(
            @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token) {
        Usuario usuario = usuarioAutenticado(token);
        return ResponseEntity.ok(listaDesejosService.listarJogosDesejados(usuario.getId()));
    }

    @PostMapping("/{jogoId}")
    public ResponseEntity<Void> adicionarJogo(
            @PathVariable UUID jogoId,
            @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token) {
        Usuario usuario = usuarioAutenticado(token);
        listaDesejosService.adicionarJogo(usuario.getId(), jogoId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{jogoId}")
    public ResponseEntity<Void> removerJogo(
            @PathVariable UUID jogoId,
            @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token) {
        Usuario usuario = usuarioAutenticado(token);
        listaDesejosService.removerJogo(usuario.getId(), jogoId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(IllegalArgumentException exception) {
        return ResponseEntity
                .badRequest()
                .body(new ErroResponse(exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(NoSuchElementException exception) {
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