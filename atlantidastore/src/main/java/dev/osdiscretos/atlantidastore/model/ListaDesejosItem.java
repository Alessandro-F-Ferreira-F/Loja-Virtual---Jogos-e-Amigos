package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lista_desejos")
public class ListaDesejosItem {
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

    protected ListaDesejosItem() {}

    public ListaDesejosItem(Usuario usuario, Jogo jogo) {
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