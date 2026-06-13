package dev.osdiscretos.atlantidastore.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @Column(columnDefinition = "TEXT")
    private UUID id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String senhaHash;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private boolean perfilPrivado = false;

    protected Usuario() {
    }

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

    public boolean isPerfilPrivado() {
        return perfilPrivado;
    }

    public void tornarPrivado() {
        this.perfilPrivado = true;
    }

    public void tornarPublico() {
        this.perfilPrivado = false;
    }
}
