package dev.osdiscretos.atlantidastore.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import dev.osdiscretos.atlantidastore.dto.StoredFile;

@Service
public class LocalArquivoStorageService implements ArquivoStorageService {

    private final Path raiz;
    private final long tamanhoMaximoBytes;

    public LocalArquivoStorageService(
        @Value("${app.storage.games-root:data/game-files}") String raiz,
        @Value("${app.storage.games-max-size-bytes:524288000}") long tamanhoMaximoBytes
    ) {
        this.raiz = Paths.get(raiz)
            .toAbsolutePath()
            .normalize();
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;

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
                .toString()
                .replace(File.separatorChar, '/');

            return new StoredFile(
                storageKey,
                nomeOriginalSeguro(arquivo),
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
        Path arquivo = caminhoSeguro(storageKey);

        if (!Files.isRegularFile(arquivo)) {
            throw new ArquivoNaoEncontradoException();
        }

        return new FileSystemResource(arquivo);
    }

    @Override
    public void removerArquivo(String storageKey) {
        Path arquivo = caminhoSeguro(storageKey);

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
        return Files.isRegularFile(caminhoSeguro(storageKey));
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException(
                "O arquivo do jogo é obrigatório"
            );
        }

        if (arquivo.getSize() <= 0) {
            throw new IllegalArgumentException(
                "O arquivo do jogo não pode estar vazio"
            );
        }

        if (arquivo.getSize() > tamanhoMaximoBytes) {
            throw new IllegalArgumentException(
                "O arquivo do jogo excede o tamanho máximo permitido"
            );
        }

        String nome = nomeOriginalSeguro(arquivo);

        if (nome == null ||
            !nome.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException(
                "O arquivo deve estar no formato ZIP"
            );
        }

        validarZipComConteudo(arquivo);
    }

    private void validarDentroDaRaiz(Path caminho) {
        if (!caminho.startsWith(raiz)) {
            throw new IllegalArgumentException(
                "Caminho de arquivo inválido"
            );
        }
    }

    private Path caminhoSeguro(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new ArquivoNaoEncontradoException();
        }

        Path caminho = raiz.resolve(storageKey).normalize();
        validarDentroDaRaiz(caminho);
        return caminho;
    }

    private String nomeOriginalSeguro(MultipartFile arquivo) {
        String nome = arquivo.getOriginalFilename();

        if (nome == null || nome.isBlank()
            || nome.contains("..")
            || nome.contains("/")
            || nome.contains("\\")) {
            throw new IllegalArgumentException("Nome de arquivo inválido");
        }

        return nome;
    }

    private void validarZipComConteudo(MultipartFile arquivo) {
        try (ZipInputStream zip = new ZipInputStream(arquivo.getInputStream())) {
            if (zip.getNextEntry() == null) {
                throw new IllegalArgumentException("O ZIP do jogo não pode estar vazio");
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("O arquivo deve ser um ZIP válido", exception);
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
        return "application/zip";
    }
}
