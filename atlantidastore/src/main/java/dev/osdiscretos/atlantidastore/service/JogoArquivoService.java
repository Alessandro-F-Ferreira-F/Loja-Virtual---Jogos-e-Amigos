package dev.osdiscretos.atlantidastore.service;

import java.util.UUID;
import java.util.NoSuchElementException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import dev.osdiscretos.atlantidastore.dto.DownloadFile;
import dev.osdiscretos.atlantidastore.dto.UploadArquivoResponseDTO;
import dev.osdiscretos.atlantidastore.repository.BibliotecaRepository;
import dev.osdiscretos.atlantidastore.repository.JogoRepository;
import dev.osdiscretos.atlantidastore.model.Jogo;
import org.springframework.stereotype.Service;
import dev.osdiscretos.atlantidastore.dto.StoredFile;
import org.springframework.core.io.Resource;

@Service
public class JogoArquivoService {

    private final JogoRepository jogoRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final ArquivoStorageService storageService;

    public JogoArquivoService(
        JogoRepository jogoRepository,
        BibliotecaRepository bibliotecaRepository,
        ArquivoStorageService storageService
    ) {
        this.jogoRepository = jogoRepository;
        this.bibliotecaRepository = bibliotecaRepository;
        this.storageService = storageService;
    }

    @Transactional
    public UploadArquivoResponseDTO enviarArquivo(
        UUID usuarioId,
        UUID jogoId,
        MultipartFile arquivo
    ) {
        Jogo jogo = buscarJogo(jogoId);

        if (!jogo.getDesenvolvedor().getId().equals(usuarioId)) {
            throw new AcessoNegadoException("Apenas o desenvolvedor pode enviar o arquivo deste jogo");
        }

        StoredFile arquivoSalvo = null;


        try {
            arquivoSalvo = storageService.salvarArquivo(jogoId, arquivo);
            
            jogo.registrarArquivo(arquivoSalvo);
            jogoRepository.save(jogo);

            return UploadArquivoResponseDTO.from(jogo);
        } catch (RuntimeException exception) {
            if (arquivoSalvo != null) {
                try {
                    storageService.removerArquivo(arquivoSalvo.storageKey());
                } catch (RuntimeException ignored) {
                    // A falha original é mais importante para o chamador.
                }
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public DownloadFile prepararDownload(UUID usuarioId, UUID jogoId) {
        Jogo jogo = buscarJogo(jogoId);

        if (!podeBaixar(usuarioId, jogo)) {
            throw new AcessoNegadoException("O usuário não tem permissão para baixar este jogo");
        }

        if (!jogo.possuiArquivo()) {
            throw new ArquivoNaoEncontradoException();
        }

        Resource resource = storageService.carregarArquivo(jogo.getArquivoStorageKey());

        return new DownloadFile(
            resource,
            jogo.getArquivoNomeOriginal(),
            jogo.getArquivoContentType(),
            jogo.getArquivoTamanhoBytes()
        );
    }

    private Jogo buscarJogo(UUID jogoId) {
        Jogo jogo = jogoRepository.findById(jogoId);

        if (jogo == null) {
            throw new NoSuchElementException("Jogo não encontrado");
        }

        return jogo;
    }

    private boolean podeBaixar(UUID usuarioId, Jogo jogo) {
        if (jogo.getDesenvolvedor().getId().equals(usuarioId)) {
            return true;
        }

        return bibliotecaRepository.existsByUsuarioIdAndJogoId(usuarioId, jogo.getId());
    }
}
