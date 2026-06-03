package dev.osdiscretos.atlantidastore.dto;

import java.util.UUID;
import java.time.LocalDateTime;
import dev.osdiscretos.atlantidastore.model.Jogo;

public record JogoResponse (
    UUID id,
    String nome,
    String descricao,
    double preco,
    LocalDateTime dataPublicacao
) { 
    public static JogoResponse from(Jogo jogo) {
        return new JogoResponse(
            jogo.getId(), 
            jogo.getNome(), 
            jogo.getDescricao(), 
            jogo.getPreco(),
            jogo.getDataPublicacao()
        );
    }    
}
