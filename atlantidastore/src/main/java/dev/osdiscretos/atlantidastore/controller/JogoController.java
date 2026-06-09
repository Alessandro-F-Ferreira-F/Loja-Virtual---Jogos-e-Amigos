package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.auth.SessionKey;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.JogoResponseDTO;
import dev.osdiscretos.atlantidastore.dto.JogoResumoDTO;
import dev.osdiscretos.atlantidastore.dto.PublicarJogoRequestDTO;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.service.AuthService;
import dev.osdiscretos.atlantidastore.service.GameImageStorageService;
import dev.osdiscretos.atlantidastore.service.JogoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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
@RequestMapping("/api/jogos")
public class JogoController {
    private final JogoService jogoService;
    private final AuthService authService;

    public JogoController(JogoService jogoService, AuthService authService) {
        this.jogoService = jogoService;
        this.authService = authService;
    }

    @GetMapping("/feed")
    public ResponseEntity<List<JogoResumoDTO>> listarFeed() {
        return ResponseEntity.ok(jogoService.listarFeed());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JogoResponseDTO> buscarDetalhes(@PathVariable UUID id) {
        return ResponseEntity.ok(jogoService.buscarDetalhes(id));
    }

    @GetMapping("/{id}/capa")
    public ResponseEntity<byte[]> buscarCapa(@PathVariable UUID id) {
        GameImageStorageService.CapaJogo capa = jogoService.buscarCapa(id);

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(capa.contentType()))
            .body(capa.bytes());
    }

    @PostMapping
    public ResponseEntity<JogoResponseDTO> publicar(
        @RequestBody PublicarJogoRequestDTO request,
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        Usuario usuario = usuarioAutenticado(token);
        JogoResponseDTO jogoCriado = jogoService.publicarJogo(usuario.getId(), request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(jogoCriado);
    }

    @GetMapping("/me/publicados")
    public ResponseEntity<List<JogoResumoDTO>> listarMeusPublicados(
        @CookieValue(name = SessionKey.COOKIE_NAME, required = false) String token
    ) {
        Usuario usuario = usuarioAutenticado(token);
        return ResponseEntity.ok(jogoService.listarJogosPublicadosPorUsuario(usuario.getId()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(IllegalArgumentException exception) {
        return ResponseEntity
            .badRequest()
            .body(new ErroResponse(exception.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroResponse> tratarJogoNaoEncontrado(NoSuchElementException exception) {
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
