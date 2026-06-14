package dev.osdiscretos.atlantidastore.dto;

import java.math.BigDecimal;

public record PublicarJogoRequestDTO(
    String nome,
    String descricao,
    BigDecimal preco,
    String tags,
    String imagemCapa
) {
}
