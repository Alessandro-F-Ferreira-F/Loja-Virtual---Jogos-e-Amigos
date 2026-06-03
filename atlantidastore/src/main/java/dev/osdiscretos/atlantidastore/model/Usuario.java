package dev.osdiscretos.atlantidastore.model;


import java.time.LocalDateTime;
import java.util.UUID;


public class Usuario {
    private final UUID id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final LocalDateTime dataCriacao;

    public Usuario(String nome, String email, String senhaHash) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.dataCriacao = LocalDateTime.now();
    }

    public Usuario(UUID id, String nome, String email, String senhaHash, LocalDateTime dataCriacao) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.dataCriacao = dataCriacao;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}
