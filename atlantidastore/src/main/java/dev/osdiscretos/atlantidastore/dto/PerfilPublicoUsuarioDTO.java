package dev.osdiscretos.atlantidastore.dto;

import dev.osdiscretos.atlantidastore.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PerfilPublicoUsuarioDTO(
    UUID id,
    String nome,
    LocalDateTime dataCriacao,
    String fotoPerfilUrl,
    List<JogoResumoDTO> jogosPublicados
) {
    public static PerfilPublicoUsuarioDTO from(Usuario usuario, List<JogoResumoDTO> jogosPublicados) {
        return new PerfilPublicoUsuarioDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getDataCriacao(),
            usuario.getFotoPerfilUrl(),
            jogosPublicados
        );
    }
}
