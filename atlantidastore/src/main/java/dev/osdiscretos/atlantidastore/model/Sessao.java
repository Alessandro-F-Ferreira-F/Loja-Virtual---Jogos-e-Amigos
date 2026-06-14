package dev.osdiscretos.atlantidastore.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    public static Sessao criarParaUsuario(String token, UUID usuarioId, int maxAgeSeconds) {
        LocalDateTime criadoEm = LocalDateTime.now();
        return new Sessao(
            token,
            usuarioId,
            criadoEm,
            criadoEm.plusSeconds(maxAgeSeconds)
        );
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