package dev.osdiscretos.atlantidastore.controller;


import dev.osdiscretos.atlantidastore.dto.CadastroRequestDTO;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.UsuarioResponse;
import dev.osdiscretos.atlantidastore.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint de cadastro de usuário
    @PostMapping
    public ResponseEntity<UsuarioResponse> register(@RequestBody CadastroRequestDTO request) {
        UsuarioResponse usuarioCriado = usuarioService.register(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(usuarioCriado);
    }

    // Endpoint de listagem de usuários
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listUsers() {
        return ResponseEntity.ok(usuarioService.listAll());
    }

    // Endpoint de remoção de usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        usuarioService.remove(id);
        return ResponseEntity.noContent().build();
    }

    // Exceptions...

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(IllegalArgumentException exception) {
        return ResponseEntity
            .badRequest()
            .body(new ErroResponse(exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroResponse> tratarUsuarioNaoEncontrado(NoSuchElementException exception) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErroResponse(exception.getMessage()));
    }
}
