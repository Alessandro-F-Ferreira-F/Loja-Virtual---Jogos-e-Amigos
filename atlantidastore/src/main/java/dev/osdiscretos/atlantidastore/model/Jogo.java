package dev.osdiscretos.atlantidastore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Jogo {
    private final UUID id;
    private final String titulo;
    private final String descricao;
    private final BigDecimal preco;
    private final UUID publicadorId;
    private final List<String> categorias;
    private final LocalDateTime dataCriacao;
    private final String downloadUrl;

    public Jogo(
        String titulo,
        String descricao,
        BigDecimal preco,
        UUID publicadorId,
        List<String> categorias,
        String downloadUrl
    ) {
        this(UUID.randomUUID(), titulo, descricao, preco, publicadorId, categorias, LocalDateTime.now(), downloadUrl);
    }

    public Jogo(
        UUID id,
        String titulo,
        String descricao,
        BigDecimal preco,
        UUID publicadorId,
        List<String> categorias,
        LocalDateTime dataCriacao,
        String downloadUrl
    ) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.preco = preco;
        this.publicadorId = publicadorId;
        this.categorias = List.copyOf(categorias);
        this.dataCriacao = dataCriacao;
        this.downloadUrl = downloadUrl;
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
        return categorias;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
