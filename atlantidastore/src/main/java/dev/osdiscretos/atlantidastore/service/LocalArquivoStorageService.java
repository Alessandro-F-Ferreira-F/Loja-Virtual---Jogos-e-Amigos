package dev.osdiscretos.atlantidastore.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.osdiscretos.atlantidastore.dto.StoredFile;

@Service
public class LocalArquivoStorageService implements ArquivoStorageService {

    private final Path raiz;

    public LocalArquivoStorageService(
        @Value("${app.storage.games-root}") String raiz
    ) {
        this.raiz = Paths.get(raiz)
            .toAbsolutePath()
            .normalize();

        criarDiretorioRaiz();
    }

    @Override
    public StoredFile salvarArquivo(
        UUID jogoId,
        MultipartFile arquivo
    ) {
        validarArquivo(arquivo);

        Path diretorioJogo = raiz
            .resolve(jogoId.toString())
            .normalize();

        Path destino = diretorioJogo
            .resolve("game.zip")
            .normalize();

        validarDentroDaRaiz(destino);

        try {
            Files.createDirectories(diretorioJogo);

            try (InputStream entrada = arquivo.getInputStream()) {
                Files.copy(
                    entrada,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }

            String storageKey = raiz
                .relativize(destino)
                .toString();

            return new StoredFile(
                storageKey,
                arquivo.getOriginalFilename(),
                determinarContentType(arquivo),
                Files.size(destino)
            );

        } catch (IOException exception) {
            throw new ArmazenamentoException(
                "Não foi possível salvar o arquivo",
                exception
            );
        }
    }

    @Override
    public Resource carregarArquivo(String storageKey) {
        Path arquivo = raiz
            .resolve(storageKey)
            .normalize();

        validarDentroDaRaiz(arquivo);

        if (!Files.exists(arquivo)) {
            throw new ArquivoNaoEncontradoException();
        }

        return new FileSystemResource(arquivo);
    }

    @Override
    public void removerArquivo(String storageKey) {
        Path arquivo = raiz
            .resolve(storageKey)
            .normalize();

        validarDentroDaRaiz(arquivo);

        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException exception) {
            throw new ArmazenamentoException(
                "Não foi possível remover o arquivo",
                exception
            );
        }
    }

    @Override
    public boolean existeArquivo(String storageKey) {
        Path arquivo = raiz
            .resolve(storageKey)
            .normalize();

        validarDentroDaRaiz(arquivo);

        return Files.isRegularFile(arquivo);
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException(
                "O arquivo do jogo é obrigatório"
            );
        }

        String nome = arquivo.getOriginalFilename();

        if (nome == null ||
            !nome.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException(
                "O arquivo deve estar no formato ZIP"
            );
        }
    }

    private void validarDentroDaRaiz(Path caminho) {
        if (!caminho.startsWith(raiz)) {
            throw new IllegalArgumentException(
                "Caminho de arquivo inválido"
            );
        }
    }

    private void criarDiretorioRaiz() {
        try {
            Files.createDirectories(raiz);
        } catch (IOException exception) {
            throw new ArmazenamentoException(
                "Não foi possível criar o diretório de armazenamento",
                exception
            );
        }
    }

    private String determinarContentType(
        MultipartFile arquivo
    ) {
        String contentType = arquivo.getContentType();

        return contentType != null
            ? contentType
            : "application/zip";
    }
}
