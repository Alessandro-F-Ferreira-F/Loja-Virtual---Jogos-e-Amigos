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

    @Column(name = "data_criacao")
    private LocalDateTime dataPublicacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PUBLICADO'")
    private StatusJogo status;

    @Column(name = "imagem_capa", columnDefinition = "TEXT")
    private String imagemCapa;

    @Column(name = "arquivo_storage_key", length = 500)
    private String arquivoStorageKey;

    @Column(name = "arquivo_nome_original", length = 255)
    private String arquivoNomeOriginal;

    @Column(name = "arquivo_content_type", length = 100)
    private String arquivoContentType;

    @Column(name = "arquivo_tamanho_bytes")
    private Long arquivoTamanhoBytes;

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
        this.dataPublicacao = null;
        this.status = StatusJogo.RASCUNHO;
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

    public Long getArquivoTamanhoBytes() {
        return arquivoTamanhoBytes;
    }

    public void registrarArquivo(StoredFile arquivo) {
        this.arquivoStorageKey = arquivo.storageKey();
        this.arquivoNomeOriginal = arquivo.nomeOriginal();
        this.arquivoContentType = arquivo.contentType();
        this.arquivoTamanhoBytes = arquivo.tamanhoBytes();
        this.dataPublicacao = LocalDateTime.now();
        this.status = StatusJogo.PUBLICADO;
    }

    public boolean possuiArquivo() {
        return arquivoStorageKey != null && !arquivoStorageKey.isBlank();
    }

}
