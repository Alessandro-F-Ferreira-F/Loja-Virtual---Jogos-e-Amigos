package dev.osdiscretos.atlantidastore.dto;

import java.util.UUID;
import java.time.LocalDateTime;
import dev.osdiscretos.atlantidastore.model.Usuario;

public record UsuarioResponse (
    UUID id,
    String nome,
    String email,
    LocalDateTime dataCriacao,
    boolean administrador,
    String fotoPerfilUrl
) { 
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(), 
            usuario.getNome(), 
            usuario.getEmail(), 
            usuario.getDataCriacao(),
            usuario.isAdministrador(),
            usuario.getFotoPerfilUrl());
    }    
}
