package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "solicitacoes_seguimento",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_solicitacao_par",
        columnNames = {"solicitante_id", "alvo_id"}
    )
)
public class SolicitacaoSeguimento {

    public enum Status {
        PENDENTE,
        ACEITA,
        RECUSADA,
        ANULADA
    }

    @Id
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @Column(name = "solicitante_id", nullable = false, columnDefinition = "TEXT")
    private UUID solicitanteId;

    @Column(name = "alvo_id", nullable = false, columnDefinition = "TEXT")
    private UUID alvoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    @Column
    private LocalDateTime resolvidaEm;

    protected SolicitacaoSeguimento() {}

    public SolicitacaoSeguimento(UUID solicitanteId, UUID alvoId) {
        this.id = UUID.randomUUID();
        this.solicitanteId = solicitanteId;
        this.alvoId = alvoId;
        this.status = Status.PENDENTE;
        this.criadaEm = LocalDateTime.now();
    }

    public void aceitar() {
        this.status = Status.ACEITA;
        this.resolvidaEm = LocalDateTime.now();
    }

    public void recusar() {
        this.status = Status.RECUSADA;
        this.resolvidaEm = LocalDateTime.now();
    }

    public void anular() {
        this.status = Status.ANULADA;
        this.resolvidaEm = LocalDateTime.now();
    }

    public boolean isPendente() {
        return this.status == Status.PENDENTE;
    }

    public UUID getId() { return id; }
    public UUID getSolicitanteId() { return solicitanteId; }
    public UUID getAlvoId() { return alvoId; }
    public Status getStatus() { return status; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public LocalDateTime getResolvidaEm() { return resolvidaEm; }
}
