package dev.osdiscretos.atlantidastore.dto;

import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.StatusJogo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record JogoResponseDTO(
    UUID id,
    String nome,
    String descricao,
    BigDecimal preco,
    String tags,
    UUID desenvolvedorId,
    String desenvolvedorNome,
    LocalDateTime dataPublicacao,
    StatusJogo status,
    String imagemCapaUrl
) {
    public static JogoResponseDTO from(Jogo jogo) {
        return new JogoResponseDTO(
            jogo.getId(),
            jogo.getNome(),
            jogo.getDescricao(),
            jogo.getPreco(),
            jogo.getTags(),
            jogo.getDesenvolvedor().getId(),
            jogo.getDesenvolvedor().getNome(),
            jogo.getDataPublicacao(),
            jogo.getStatus(),
            jogo.getImagemCapa()
        );
    }
}
