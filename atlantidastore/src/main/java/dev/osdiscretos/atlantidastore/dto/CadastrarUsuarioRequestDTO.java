package dev.osdiscretos.atlantidastore.dto;


public record CadastrarUsuarioRequestDTO (
    String nome,
    String email
) { }