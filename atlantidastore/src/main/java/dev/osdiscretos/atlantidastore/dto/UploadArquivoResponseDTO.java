package dev.osdiscretos.atlantidastore.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.StatusJogo;


public record UploadArquivoResponseDTO(
    UUID jogoId,
    String nomeArquivo,
    long tamanhoBytes,
    StatusJogo status,
    LocalDateTime dataPublicacao,
    String mensagem
) {
    public static UploadArquivoResponseDTO from(
        Jogo jogo
    ) {
        return new UploadArquivoResponseDTO(
            jogo.getId(),
            jogo.getArquivoNomeOriginal(),
            Long.parseLong(jogo.getArquivoTamanhoBytes()),
            jogo.getStatus(),
            jogo.getDataPublicacao(),
            "Arquivo enviado com sucesso!"
        );
    }
}