package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.service.GameImageStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/game_images")
public class GameImageController {
    private final GameImageStorageService gameImageStorageService;

    public GameImageController(GameImageStorageService gameImageStorageService) {
        this.gameImageStorageService = gameImageStorageService;
    }

    @GetMapping("/{nomeArquivo:.+}")
    public ResponseEntity<byte[]> buscarImagem(@PathVariable String nomeArquivo) {
        GameImageStorageService.CapaJogo capa = gameImageStorageService.buscarPorNomeArquivo(nomeArquivo);

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(capa.contentType()))
            .body(capa.bytes());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> tratarImagemNaoEncontrada() {
        return ResponseEntity.notFound().build();
    }
}
