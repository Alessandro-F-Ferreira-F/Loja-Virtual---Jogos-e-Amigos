package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "seguimentos",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_seguimento_par",
        columnNames = {"seguidor_id", "seguido_id"}
    )
)
public class Seguimento {

    @Id
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @Column(name = "seguidor_id", nullable = false, columnDefinition = "TEXT")
    private UUID seguidorId;

    @Column(name = "seguido_id", nullable = false, columnDefinition = "TEXT")
    private UUID seguidoId;

    @Column(nullable = false)
    private LocalDateTime seguidoEm;

    protected Seguimento() {}

    public Seguimento(UUID seguidorId, UUID seguidoId) {
        this.id = UUID.randomUUID();
        this.seguidorId = seguidorId;
        this.seguidoId = seguidoId;
        this.seguidoEm = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getSeguidorId() { return seguidorId; }
    public UUID getSeguidoId() { return seguidoId; }
    public LocalDateTime getSeguidoEm() { return seguidoEm; }
}
