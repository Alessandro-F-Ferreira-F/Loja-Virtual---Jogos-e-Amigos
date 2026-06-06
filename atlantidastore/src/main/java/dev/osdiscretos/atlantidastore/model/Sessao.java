package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessoes")
public class Sessao {
    @Id
    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false, columnDefinition = "TEXT")
    private UUID usuarioId;

    @Column(nullable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime expiraEm;

    protected Sessao() {}

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
