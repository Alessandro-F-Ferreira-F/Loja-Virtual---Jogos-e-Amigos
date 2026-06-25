package dev.osdiscretos.atlantidastore.controller;


import dev.osdiscretos.atlantidastore.auth.SessionKey;
import dev.osdiscretos.atlantidastore.dto.CadastroRequestDTO;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.UsuarioResponse;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.service.AuthService;
import dev.osdiscretos.atlantidastore.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final AuthService authService;

    public UsuarioController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
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
    public ResponseEntity<List<UsuarioResponse>> listUsers(
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        return ResponseEntity.ok(usuarioService.listAll());
    }

    @PostMapping(path = "/me/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioResponse> atualizarFotoPerfil(
        @RequestParam("foto") MultipartFile foto,
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        Usuario usuario = usuarioAutenticado(token);
        return ResponseEntity.ok(usuarioService.atualizarFotoPerfil(usuario.getId(), foto));
    }

    // Endpoint de remoção de usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(
        @PathVariable UUID id,
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
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

    private Usuario usuarioAutenticado(String token) {
        Usuario usuario = authService.findUserBySessionToken(token);

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login obrigatório");
        }

        return usuario;
    }

}
