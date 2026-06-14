package dev.osdiscretos.atlantidastore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "jogos")
public class Jogo {
    @Id
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 255)
    private String nome;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "categorias", nullable = false, columnDefinition = "TEXT")
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicador_id", nullable = false)
    private Usuario desenvolvedor;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataPublicacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PUBLICADO'")
    private StatusJogo status;

    @Column(name = "imagem_capa", columnDefinition = "TEXT")
    private String imagemCapa;

    @Column(name = "download_url", nullable = false, length = 500)
    private String downloadUrl;

    protected Jogo() {}

    public Jogo(
        String nome,
        String descricao,
        BigDecimal preco,
        String tags,
        Usuario desenvolvedor,
        String imagemCapa
    ) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.tags = tags;
        this.desenvolvedor = desenvolvedor;
        this.dataPublicacao = LocalDateTime.now();
        this.status = StatusJogo.PUBLICADO;
        this.imagemCapa = imagemCapa;
        this.downloadUrl = "";
    }

    public static Jogo criarPublicado(
        String nome,
        String descricao,
        BigDecimal preco,
        String tags,
        Usuario desenvolvedor
    ) {
        return new Jogo(
            nome,
            descricao,
            preco,
            tags,
            desenvolvedor,
            null
        );
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public String getTags() {
        return tags;
    }

    public Usuario getDesenvolvedor() {
        return desenvolvedor;
    }

    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }

    public StatusJogo getStatus() {
        return status;
    }

    public String getImagemCapa() {
        return imagemCapa;
    }

    public void setImagemCapa(String imagemCapa) {
        this.imagemCapa = imagemCapa;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}