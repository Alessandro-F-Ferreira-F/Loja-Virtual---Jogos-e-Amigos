package dev.osdiscretos.atlantidastore.dto;

import org.springframework.core.io.Resource;

public record DownloadFile(
    Resource resource,
    String nomeArquivo,
    String contentType,
    long tamanhoBytes
) {
}