package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.service.ProfileImageStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/profile_images")
public class ProfileImageController {
    private final ProfileImageStorageService profileImageStorageService;

    public ProfileImageController(ProfileImageStorageService profileImageStorageService) {
        this.profileImageStorageService = profileImageStorageService;
    }

    @GetMapping("/{nomeArquivo:.+}")
    public ResponseEntity<byte[]> buscarImagem(@PathVariable String nomeArquivo) {
        ProfileImageStorageService.FotoPerfil foto = profileImageStorageService.buscarPorNomeArquivo(nomeArquivo);

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(foto.contentType()))
            .body(foto.bytes());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> tratarImagemNaoEncontrada() {
        return ResponseEntity.notFound().build();
    }
}
