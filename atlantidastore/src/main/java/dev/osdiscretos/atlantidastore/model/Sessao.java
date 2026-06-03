package dev.osdiscretos.atlantidastore.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Sessao {
    private final String token;
    private final UUID usuarioId;
    private final LocalDateTime criadoEm;
    private final LocalDateTime expiraEm;

    public Sessao(String token, UUID usuarioId, LocalDateTime criadoEm, LocalDateTime expiraEm) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
    }

    public String getToken() {
        return token;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public boolean isExpirada() {
        return LocalDateTime.now().isAfter(expiraEm);
    }
}
