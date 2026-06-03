package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jogos")
public class Jogo {
    @Id
    private UUID id;
    private String nome;
    private String descricao;
    private double preco;
    private LocalDateTime dataPublicacao;

    protected Jogo() {} // Construtor exigido pelo JPA

    public Jogo(String nome, String descricao, double preco) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.dataPublicacao = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }
}
