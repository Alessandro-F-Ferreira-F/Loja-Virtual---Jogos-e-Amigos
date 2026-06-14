package dev.osdiscretos.atlantidastore.dto;

import dev.osdiscretos.atlantidastore.model.Jogo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record JogoResponse(
    UUID id,
    String titulo,
    String descricao,
    BigDecimal preco,
    UUID publicadorId,
    List<String> categorias,
    LocalDateTime dataCriacao,
    String downloadUrl,
    String imagemCapa
) {
    public static JogoResponse from(Jogo jogo) {
        return new JogoResponse(
            jogo.getId(),
            jogo.getNome(),
            jogo.getDescricao(),
            jogo.getPreco(),
            jogo.getDesenvolvedor() != null ? jogo.getDesenvolvedor().getId() : null,
            jogo.getTags() != null && !jogo.getTags().isBlank() ? Arrays.asList(jogo.getTags().split("\\|")) : List.of(),
            jogo.getDataPublicacao(),
            jogo.getDownloadUrl(),
            jogo.getImagemCapa()
        );
    }
}
