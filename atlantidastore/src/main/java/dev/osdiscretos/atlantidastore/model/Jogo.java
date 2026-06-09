package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "jogos")
public class Jogo {
    @Id
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false, columnDefinition = "TEXT")
    private UUID publicadorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String categorias;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false, length = 500)
    private String downloadUrl;

    @Column(columnDefinition = "TEXT")
    private String imagemCapa;

    protected Jogo() {}

    public Jogo(
        String titulo,
        String descricao,
        BigDecimal preco,
        UUID publicadorId,
        List<String> categorias,
        String downloadUrl,
        String imagemCapa
    ) {
        this(UUID.randomUUID(), titulo, descricao, preco, publicadorId, categorias, LocalDateTime.now(), downloadUrl, imagemCapa);
    }

    public Jogo(
        UUID id,
        String titulo,
        String descricao,
        BigDecimal preco,
        UUID publicadorId,
        List<String> categorias,
        LocalDateTime dataCriacao,
        String downloadUrl,
        String imagemCapa
    ) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.preco = preco;
        this.publicadorId = publicadorId;
        this.categorias = String.join("|", categorias);
        this.dataCriacao = dataCriacao;
        this.downloadUrl = downloadUrl;
        this.imagemCapa = imagemCapa;
    }

    public UUID getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public UUID getPublicadorId() {
        return publicadorId;
    }

    public List<String> getCategorias() {
        if (categorias == null || categorias.isBlank()) {
            return List.of();
        }
        return Arrays.stream(categorias.split("\\|"))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .collect(Collectors.toList());
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getImagemCapa() {
        return imagemCapa;
    }
}
