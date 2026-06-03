package dev.osdiscretos.atlantidastore.dto;

public record CadastrarJogoRequestDTO (
    String nome,
    String descricao,
    double preco
) { }
