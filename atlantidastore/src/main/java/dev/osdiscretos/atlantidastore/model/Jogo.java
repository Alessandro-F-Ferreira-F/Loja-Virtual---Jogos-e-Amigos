package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import dev.osdiscretos.atlantidastore.dto.StoredFile;

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

    private String arquivoStorageKey;

    private String arquivoNomeOriginal;

    private String arquivoContentType;

    private String arquivoTamanhoBytes;

    

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
        this.arquivoStorageKey = null;
        this.arquivoNomeOriginal = null;
        this.arquivoContentType = null;
        this.arquivoTamanhoBytes = null;
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

    public String getArquivoStorageKey() {
        return arquivoStorageKey;
    }

    public String getArquivoNomeOriginal() {
        return arquivoNomeOriginal;
    }

    public String getArquivoContentType() {
        return arquivoContentType;
    }

    public String getArquivoTamanhoBytes() {
        return arquivoTamanhoBytes;
    }

    public void registrarArquivo(StoredFile arquivo) {
        this.arquivoStorageKey = arquivo.storageKey();
        this.arquivoNomeOriginal = arquivo.nomeOriginal();
        this.arquivoContentType = arquivo.contentType();
        this.arquivoTamanhoBytes = arquivo.tamanhoBytes().toString();
        this.dataPublicacao = LocalDateTime.now();
        this.status = StatusJogo.PUBLICADO;
    }

    public boolean possuiArquivo() {
        return arquivoStorageKey != null && !arquivoStorageKey.isBlank();
    }

}
