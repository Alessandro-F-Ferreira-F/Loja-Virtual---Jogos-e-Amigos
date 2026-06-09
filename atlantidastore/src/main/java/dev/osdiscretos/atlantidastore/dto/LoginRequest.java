package dev.osdiscretos.atlantidastore.dto;


public record LoginRequest(
    String email,
    String senha
) {}