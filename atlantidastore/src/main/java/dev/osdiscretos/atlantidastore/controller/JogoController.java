package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.dto.CadastrarJogoRequestDTO;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.JogoResponse;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.service.JogoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/jogos")
public class JogoController {
    private final JogoService jogoService;

    public JogoController(JogoService jogoService) {
        this.jogoService = jogoService;
    }

    @PostMapping
    public ResponseEntity<JogoResponse> cadastrar(
        @RequestBody CadastrarJogoRequestDTO request,
        @RequestAttribute("usuarioLogado") Usuario usuarioLogado
    ) {
        JogoResponse jogoCriado = jogoService.cadastrar(request, usuarioLogado);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(jogoCriado);
    }

    @GetMapping
    public ResponseEntity<List<JogoResponse>> listar() {
        return ResponseEntity.ok(jogoService.listar());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        jogoService.remover(id);
        return ResponseEntity.noContent().build();
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
}
