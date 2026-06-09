package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "biblioteca_itens",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_biblioteca_usuario_jogo",
        columnNames = {"usuario_id", "jogo_id"}
    )
)
public class BibliotecaItem {
    @Id
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jogo_id", nullable = false)
    private Jogo jogo;

    @Column(name = "data_adicao", nullable = false)
    private LocalDateTime dataAdicao;

    protected BibliotecaItem() {}

    public BibliotecaItem(Usuario usuario, Jogo jogo) {
        this.id = UUID.randomUUID();
        this.usuario = usuario;
        this.jogo = jogo;
        this.dataAdicao = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public LocalDateTime getDataAdicao() {
        return dataAdicao;
    }
}
