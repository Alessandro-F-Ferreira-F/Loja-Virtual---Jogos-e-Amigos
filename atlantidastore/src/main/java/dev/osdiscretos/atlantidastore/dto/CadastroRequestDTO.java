package dev.osdiscretos.atlantidastore.dto;


public record CadastroRequestDTO (
    String nome,
    String email,
    String senha
) { }