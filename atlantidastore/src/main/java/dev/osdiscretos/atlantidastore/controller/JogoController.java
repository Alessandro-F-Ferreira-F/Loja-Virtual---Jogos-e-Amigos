package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.dto.CadastrarJogoRequestDTO;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.JogoResponse;
import dev.osdiscretos.atlantidastore.service.JogoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        @RequestBody CadastrarJogoRequestDTO request
    ) {
        JogoResponse jogoCriado = jogoService.cadastrar(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(jogoCriado);
    }

    @GetMapping
    public ResponseEntity<List<JogoResponse>> listar() {
        List<JogoResponse> jogos = jogoService.listar();

        return ResponseEntity.ok(jogos);
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
