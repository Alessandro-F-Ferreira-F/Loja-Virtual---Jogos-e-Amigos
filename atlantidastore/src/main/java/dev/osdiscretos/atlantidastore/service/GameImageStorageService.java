package dev.osdiscretos.atlantidastore.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class GameImageStorageService {
    private static final Path GAME_IMAGES_DIR = Path.of("game_images");
    private static final int TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
        "image/png",
        "image/jpeg",
        "image/gif",
        "image/webp"
    );
    private static final Map<String, String> EXTENSOES = Map.of(
        "image/png", ".png",
        "image/jpeg", ".jpg",
        "image/gif", ".gif",
        "image/webp", ".webp"
    );

    public String salvarDataUrl(String dataUrl, UUID jogoId) {
        String normalizada = normalize(dataUrl);

        if (normalizada.isBlank()) {
            return null;
        }

        ImagemDecodificada imagem = decodificarDataUrl(normalizada);
        String nomeArquivo = jogoId + EXTENSOES.get(imagem.contentType());
        Path destino = diretorioImagens().resolve(nomeArquivo);

        try {
            Files.createDirectories(diretorioImagens());
            Files.write(destino, imagem.bytes());
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível salvar a capa do jogo", exception);
        }

        return "/game_images/" + nomeArquivo;
    }

    public boolean isDataUrl(String imagemCapa) {
        return normalize(imagemCapa).startsWith("data:");
    }

    public CapaJogo buscarPorUrl(String imagemCapaUrl) {
        String normalizada = normalize(imagemCapaUrl);

        if (normalizada.isBlank() || !normalizada.startsWith("/game_images/")) {
            throw new NoSuchElementException("Capa do jogo não encontrada");
        }

        return buscarPorNomeArquivo(normalizada.substring("/game_images/".length()));
    }

    public CapaJogo buscarPorNomeArquivo(String nomeArquivo) {
        String nomeSeguro = normalize(nomeArquivo);

        if (nomeSeguro.isBlank()
            || nomeSeguro.contains("/")
            || nomeSeguro.contains("\\")
            || nomeSeguro.contains("..")) {
            throw new NoSuchElementException("Capa do jogo não encontrada");
        }

        Path arquivo = diretorioImagens().resolve(nomeSeguro).normalize();

        if (!arquivo.startsWith(diretorioImagens())) {
            throw new NoSuchElementException("Capa do jogo não encontrada");
        }

        try {
            if (!Files.exists(arquivo) || !Files.isRegularFile(arquivo)) {
                throw new NoSuchElementException("Capa do jogo não encontrada");
            }

            return new CapaJogo(contentType(nomeSeguro), Files.readAllBytes(arquivo));
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler a capa do jogo", exception);
        }
    }

    private ImagemDecodificada decodificarDataUrl(String dataUrl) {
        int separatorIndex = dataUrl.indexOf(',');

        if (separatorIndex < 0) {
            throw new IllegalArgumentException("Capa do jogo está em formato inválido");
        }

        String metadata = dataUrl.substring("data:".length(), separatorIndex).toLowerCase(Locale.ROOT);
        String payload = dataUrl.substring(separatorIndex + 1);
        String contentType = metadata.split(";")[0];

        if (!metadata.contains(";base64")) {
            throw new IllegalArgumentException("Capa do jogo deve estar em base64");
        }

        if (!TIPOS_PERMITIDOS.contains(contentType)) {
            throw new IllegalArgumentException("Capa deve ser PNG, JPEG, GIF ou WebP");
        }

        byte[] bytes;

        try {
            bytes = Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Capa do jogo está em base64 inválido", exception);
        }

        if (bytes.length > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("Capa do jogo deve ter no máximo 5 MB");
        }

        return new ImagemDecodificada(contentType, bytes);
    }

    private String contentType(String nomeArquivo) {
        String nome = nomeArquivo.toLowerCase(Locale.ROOT);

        if (nome.endsWith(".png")) {
            return "image/png";
        }

        if (nome.endsWith(".jpg") || nome.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (nome.endsWith(".gif")) {
            return "image/gif";
        }

        if (nome.endsWith(".webp")) {
            return "image/webp";
        }

        return "application/octet-stream";
    }

    private Path diretorioImagens() {
        return GAME_IMAGES_DIR.toAbsolutePath().normalize();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record CapaJogo(String contentType, byte[] bytes) {
    }

    private record ImagemDecodificada(String contentType, byte[] bytes) {
    }
}
