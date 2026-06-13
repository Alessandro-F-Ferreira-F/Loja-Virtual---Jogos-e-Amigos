package dev.osdiscretos.atlantidastore.dto;

import dev.osdiscretos.atlantidastore.model.Notificacao;
import dev.osdiscretos.atlantidastore.model.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificacaoResponse(
    UUID id,
    String tipo,
    UUID atorId,
    String atorNome,
    UUID referenciaId,
    boolean lida,
    LocalDateTime criadaEm
) {
    public static NotificacaoResponse from(Notificacao notificacao, Usuario ator) {
        return new NotificacaoResponse(
            notificacao.getId(),
            notificacao.getTipo().name(),
            ator.getId(),
            ator.getNome(),
            notificacao.getReferenciaId(),
            notificacao.isLida(),
            notificacao.getCriadaEm()
        );
    }
}
