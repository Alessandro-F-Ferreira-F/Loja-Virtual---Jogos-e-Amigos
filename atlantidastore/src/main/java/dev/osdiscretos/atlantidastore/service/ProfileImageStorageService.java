package dev.osdiscretos.atlantidastore.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class ProfileImageStorageService {
    private static final Path PROFILE_IMAGES_DIR = Path.of("profile_images");
    private static final int TAMANHO_MAXIMO_BYTES = 2 * 1024 * 1024;
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

    public String salvarFoto(UUID usuarioId, MultipartFile foto) {
        validarFoto(foto);

        String contentType = foto.getContentType().toLowerCase(Locale.ROOT);
        String nomeArquivo = usuarioId + EXTENSOES.get(contentType);
        Path destino = diretorioImagens().resolve(nomeArquivo);

        try {
            Files.createDirectories(diretorioImagens());

            try (InputStream entrada = foto.getInputStream()) {
                Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível salvar a foto de perfil", exception);
        }

        return "/profile_images/" + nomeArquivo;
    }

    public FotoPerfil buscarPorNomeArquivo(String nomeArquivo) {
        String nomeSeguro = normalize(nomeArquivo);

        if (nomeSeguro.isBlank()
            || nomeSeguro.contains("/")
            || nomeSeguro.contains("\\")
            || nomeSeguro.contains("..")) {
            throw new NoSuchElementException("Foto de perfil não encontrada");
        }

        Path arquivo = diretorioImagens().resolve(nomeSeguro).normalize();

        if (!arquivo.startsWith(diretorioImagens())) {
            throw new NoSuchElementException("Foto de perfil não encontrada");
        }

        try {
            if (!Files.isRegularFile(arquivo)) {
                throw new NoSuchElementException("Foto de perfil não encontrada");
            }

            return new FotoPerfil(contentType(nomeSeguro), Files.readAllBytes(arquivo));
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler a foto de perfil", exception);
        }
    }

    private void validarFoto(MultipartFile foto) {
        if (foto == null || foto.isEmpty() || foto.getSize() <= 0) {
            throw new IllegalArgumentException("A foto de perfil é obrigatória");
        }

        if (foto.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("A foto de perfil deve ter no máximo 2 MB");
        }

        String contentType = foto.getContentType();

        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("A foto de perfil deve ser PNG, JPEG, GIF ou WebP");
        }
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
        return PROFILE_IMAGES_DIR.toAbsolutePath().normalize();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    public record FotoPerfil(String contentType, byte[] bytes) {
    }
}
