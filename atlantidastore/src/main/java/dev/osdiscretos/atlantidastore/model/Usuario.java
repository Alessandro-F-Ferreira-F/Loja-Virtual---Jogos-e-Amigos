package dev.osdiscretos.atlantidastore.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    private UUID id;
    private String nome;
    private String email;
    private LocalDateTime dataCriacao;

    // Configurações de Privacidade (Sugestão 4)
    private boolean perfilPublico;
    private boolean jogosAdquiridosPublicos;

    protected Usuario() {} // Construtor padrão exigido pelo JPA

    public Usuario(String nome, String email) {
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.email = email;
        this.dataCriacao = LocalDateTime.now();
        
        // Padrões de privacidade iniciais
        this.perfilPublico = true;
        this.jogosAdquiridosPublicos = false;
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

    public boolean isPerfilPublico() {
        return perfilPublico;
    }

    public void setPerfilPublico(boolean perfilPublico) {
        this.perfilPublico = perfilPublico;
    }

    public boolean isJogosAdquiridosPublicos() {
        return jogosAdquiridosPublicos;
    }

    public void setJogosAdquiridosPublicos(boolean jogosAdquiridosPublicos) {
        this.jogosAdquiridosPublicos = jogosAdquiridosPublicos;
    }
}