package dev.osdiscretos.atlantidastore.dto;

import dev.osdiscretos.atlantidastore.model.Usuario;

import java.util.UUID;

public record SeguimentoResponse(
    UUID id,
    String nome,
    String email
) {
    public static SeguimentoResponse from(Usuario usuario) {
        return new SeguimentoResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail()
        );
    }
}
