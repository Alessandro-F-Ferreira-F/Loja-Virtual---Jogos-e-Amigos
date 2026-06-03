package dev.osdiscretos.atlantidastore.model;


import java.time.LocalDateTime;
import java.util.UUID;


public class Usuario {
    private final UUID id;
    private String nome;
    private String email;
    private final LocalDateTime dataCriacao;

    public Usuario(String nome, String email) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.email = email;
        this.dataCriacao = LocalDateTime.now();
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

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
}