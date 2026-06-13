package dev.osdiscretos.atlantidastore.dto;

import dev.osdiscretos.atlantidastore.model.SolicitacaoSeguimento;
import dev.osdiscretos.atlantidastore.model.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record SolicitacaoSeguimentoResponse(
    UUID id,
    UUID solicitanteId,
    String solicitanteNome,
    String solicitanteEmail,
    LocalDateTime criadaEm
) {
    public static SolicitacaoSeguimentoResponse from(SolicitacaoSeguimento solicitacao, Usuario solicitante) {
        return new SolicitacaoSeguimentoResponse(
            solicitacao.getId(),
            solicitante.getId(),
            solicitante.getNome(),
            solicitante.getEmail(),
            solicitacao.getCriadaEm()
        );
    }
}
