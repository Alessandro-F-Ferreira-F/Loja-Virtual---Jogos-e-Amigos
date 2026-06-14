package dev.osdiscretos.atlantidastore.dto;

public record StoredFile (
    String storageKey,
    String nomeOriginal,
    String contentType,
    Long tamanhoBytes
) { }
