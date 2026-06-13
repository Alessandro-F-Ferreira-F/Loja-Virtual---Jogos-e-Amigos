package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificacoes")
public class Notificacao {

    public enum Tipo {
        SOLICITACAO_SEGUIMENTO,
        NOVO_SEGUIDOR,
        SOLICITACAO_ACEITA
    }

    @Id
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private UUID destinatarioId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private UUID atorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Tipo tipo;

    @Column(columnDefinition = "TEXT")
    private UUID referenciaId;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    protected Notificacao() {
    }

    public Notificacao(UUID destinatarioId, UUID atorId, Tipo tipo, UUID referenciaId) {
        this.id = UUID.randomUUID();
        this.destinatarioId = destinatarioId;
        this.atorId = atorId;
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.criadaEm = LocalDateTime.now();
    }

    public void marcarComoLida() {
        this.lida = true;
    }

    public UUID getId() { return id; }
    public UUID getDestinatarioId() { return destinatarioId; }
    public UUID getAtorId() { return atorId; }
    public Tipo getTipo() { return tipo; }
    public UUID getReferenciaId() { return referenciaId; }
    public boolean isLida() { return lida; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}
