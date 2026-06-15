package dev.osdiscretos.atlantidastore.dto;

import dev.osdiscretos.atlantidastore.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PerfilUsuarioDTO(
    UUID id,
    String nome,
    String email,
    LocalDateTime dataCriacao,
    boolean perfilPrivado,
    String fotoPerfilUrl,
    List<JogoResumoDTO> jogosPublicados,
    List<JogoResumoDTO> biblioteca
) {
    public static PerfilUsuarioDTO from(
        Usuario usuario,
        List<JogoResumoDTO> jogosPublicados,
        List<JogoResumoDTO> biblioteca
    ) {
        return new PerfilUsuarioDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getDataCriacao(),
            usuario.isPerfilPrivado(),
            usuario.getFotoPerfilUrl(),
            jogosPublicados,
            biblioteca
        );
    }
}
