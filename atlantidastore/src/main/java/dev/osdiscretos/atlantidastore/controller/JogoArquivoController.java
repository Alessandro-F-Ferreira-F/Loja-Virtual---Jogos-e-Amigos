package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.dto.UploadArquivoResponseDTO;
import dev.osdiscretos.atlantidastore.service.JogoArquivoService;
import dev.osdiscretos.atlantidastore.service.AuthService;
import dev.osdiscretos.atlantidastore.auth.SessionKey;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.dto.DownloadFile;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import java.util.UUID;

@RestController
@RequestMapping("/api/jogos")
public class JogoArquivoController {

    private final AuthService authService;
    private final JogoArquivoService jogoArquivoService;

    public JogoArquivoController(
        AuthService authService,
        JogoArquivoService jogoArquivoService
    ) {
        this.authService = authService;
        this.jogoArquivoService = jogoArquivoService;
    }

    @PostMapping(
        path = "/{jogoId}/arquivo",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UploadArquivoResponseDTO> upload(
        @PathVariable UUID jogoId,
        @RequestParam("arquivo") MultipartFile arquivo,
        @CookieValue(name = SessionKey.COOKIE_NAME) String token
    ) {
        Usuario usuario = authService.findUserBySessionToken(token);

        UploadArquivoResponseDTO response = jogoArquivoService.enviarArquivo(usuario.getId(),jogoId,arquivo);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{jogoId}/download")
    public ResponseEntity<Resource> download(
        @PathVariable UUID jogoId,
        @CookieValue(name = SessionKey.COOKIE_NAME) String token
    ) {
        Usuario usuario = authService.findUserBySessionToken(token);

        DownloadFile download = jogoArquivoService.prepararDownload(usuario.getId(), jogoId);

        MediaType mediaType = MediaType.parseMediaType(download.contentType());

      return ResponseEntity.ok()
        .contentType(mediaType)
        .contentLength(download.tamanhoBytes())
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition
                .attachment()
                .filename(download.nomeArquivo())
                .build()
                .toString()
        )
        .body(download.resource());
    }
}