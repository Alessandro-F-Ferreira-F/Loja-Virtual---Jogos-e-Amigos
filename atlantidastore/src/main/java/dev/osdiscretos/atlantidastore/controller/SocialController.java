package dev.osdiscretos.atlantidastore.controller;

import dev.osdiscretos.atlantidastore.dto.AlterarPrivacidadeRequestDTO;
import dev.osdiscretos.atlantidastore.dto.ErroResponse;
import dev.osdiscretos.atlantidastore.dto.RelacionamentoResponse;
import dev.osdiscretos.atlantidastore.dto.SeguimentoResponse;
import dev.osdiscretos.atlantidastore.dto.SolicitacaoSeguimentoResponse;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.service.SocialService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    @PostMapping("/api/usuarios/{id}/seguir")
    public ResponseEntity<RelacionamentoResponse> seguir(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        Usuario me = usuarioLogado(request);
        RelacionamentoResponse resposta = socialService.seguir(me, id);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/api/usuarios/{id}/seguir")
    public ResponseEntity<Void> deixarDeSeguir(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        Usuario me = usuarioLogado(request);
        socialService.deixarDeSeguir(me, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/usuarios/{id}/solicitacao")
    public ResponseEntity<Void> cancelarSolicitacao(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        Usuario me = usuarioLogado(request);
        socialService.cancelarSolicitacao(me, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/usuarios/{id}/seguidores")
    public ResponseEntity<List<SeguimentoResponse>> listarSeguidores(@PathVariable UUID id) {
        return ResponseEntity.ok(socialService.listarSeguidores(id));
    }

    @GetMapping("/api/usuarios/{id}/seguindo")
    public ResponseEntity<List<SeguimentoResponse>> listarSeguindo(@PathVariable UUID id) {
        return ResponseEntity.ok(socialService.listarSeguindo(id));
    }

    @GetMapping("/api/usuarios/{id}/seguir")
    public ResponseEntity<RelacionamentoResponse> statusRelacionamento(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        Usuario me = usuarioLogado(request);
        return ResponseEntity.ok(socialService.statusRelacionamento(me, id));
    }

    @GetMapping("/api/solicitacoes")
    public ResponseEntity<List<SolicitacaoSeguimentoResponse>> listarSolicitacoes(
        HttpServletRequest request
    ) {
        Usuario me = usuarioLogado(request);
        return ResponseEntity.ok(socialService.listarSolicitacoesPendentes(me));
    }

    @PostMapping("/api/solicitacoes/{id}/aceitar")
    public ResponseEntity<Void> aceitarSolicitacao(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        Usuario me = usuarioLogado(request);
        socialService.aceitarSolicitacao(me, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/solicitacoes/{id}/recusar")
    public ResponseEntity<Void> recusarSolicitacao(
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        Usuario me = usuarioLogado(request);
        socialService.recusarSolicitacao(me, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/usuarios/me/privacidade")
    public ResponseEntity<Void> alterarPrivacidade(
        @RequestBody AlterarPrivacidadeRequestDTO request,
        HttpServletRequest httpRequest
    ) {
        Usuario me = usuarioLogado(httpRequest);
        socialService.alterarPrivacidade(me, request.perfilPrivado());
        return ResponseEntity.noContent().build();
    }

    private Usuario usuarioLogado(HttpServletRequest request) {
        return (Usuario) request.getAttribute("usuarioLogado");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResponse> tratarEstadoInvalido(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResponse(ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResponse(ex.getMessage()));
    }
}
