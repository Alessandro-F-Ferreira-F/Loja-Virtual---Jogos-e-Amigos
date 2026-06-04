package dev.osdiscretos.atlantidastore.dto;

import java.math.BigDecimal;
import java.util.List;

public record CadastrarJogoRequestDTO(
    String titulo,
    String descricao,
    BigDecimal preco,
    List<String> categorias,
    String downloadUrl
) {
}
