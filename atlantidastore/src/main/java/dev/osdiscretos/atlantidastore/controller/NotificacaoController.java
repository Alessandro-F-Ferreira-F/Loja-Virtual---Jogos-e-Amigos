package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.NotificacaoResponse;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.service.NotificacaoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoResponse>> listar(HttpServletRequest request) {
        Usuario me = usuarioLogado(request);
        return ResponseEntity.ok(notificacaoService.listar(me));
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(HttpServletRequest request) {
        Usuario me = usuarioLogado(request);
        long count = notificacaoService.contarNaoLidas(me);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<Void> marcarComoLida(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        Usuario me = usuarioLogado(request);
        notificacaoService.marcarComoLida(me, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/lidas")
    public ResponseEntity<Void> marcarTodasComoLidas(HttpServletRequest request) {
        Usuario me = usuarioLogado(request);
        notificacaoService.marcarTodasComoLidas(me);
        return ResponseEntity.noContent().build();
    }

    private Usuario usuarioLogado(HttpServletRequest request) {
        return (Usuario) request.getAttribute("usuarioLogado");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResponse(ex.getMessage()));
    }
}
