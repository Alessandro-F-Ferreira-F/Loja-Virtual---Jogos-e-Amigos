package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.auth.SessionKey;
import dev.osdiscretos.atlantidastore.dto.CadastroRequestDTO;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.JogoResumoDTO;
import dev.osdiscretos.atlantidastore.dto.UsuarioResponse;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.service.AuthService;
import dev.osdiscretos.atlantidastore.service.JogoService;
import dev.osdiscretos.atlantidastore.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final JogoService jogoService;

    public AdminController(
        AuthService authService,
        UsuarioService usuarioService,
        JogoService jogoService
    ) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.jogoService = jogoService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios(
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        exigirAdmin(token);
        return ResponseEntity.ok(usuarioService.listAll());
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> criarUsuario(
        @RequestBody CadastroRequestDTO request,
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        exigirAdmin(token);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(usuarioService.register(request));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> removerUsuario(
        @PathVariable UUID id,
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        Usuario admin = exigirAdmin(token);

        if (admin.getId().equals(id)) {
            throw new IllegalArgumentException("O administrador não pode remover a própria conta");
        }

        usuarioService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/jogos")
    public ResponseEntity<List<JogoResumoDTO>> listarJogos(
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        exigirAdmin(token);
        return ResponseEntity.ok(jogoService.listarFeed());
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

    private Usuario exigirAdmin(String token) {
        Usuario usuario = authService.findUserBySessionToken(token);

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login obrigatório");
        }

        if (!usuario.isAdministrador()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso administrativo obrigatório");
        }

        return usuario;
    }
}
