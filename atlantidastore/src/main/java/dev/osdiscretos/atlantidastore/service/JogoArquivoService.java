package dev.osdiscretos.atlantidastore.service;

import java.util.UUID;
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
        Jogo jogo = jogoRepository.findById(jogoId).orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado"));

        StoredFile arquivoSalvo = null;


        try {
            arquivoSalvo = storageService.salvarArquivo(jogoId, arquivo);
            
            jogo.registrarArquivo(arquivoSalvo);
            jogoRepository.save(jogo);

            return UploadArquivoResponseDTO.from(jogo);
        } catch (RuntimeException exception) {
            if (arquivoSalvo != null) {
                storageService.removerArquivo(arquivoSalvo.storageKey());
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public DownloadFile prepararDownload(UUID usuarioId,UUID jogoId) {
        Jogo jogo = jogoRepository.findById(jogoId);

        boolean possuiNaBiblioteca = bibliotecaRepository.existsByUsuarioIdAndJogoId(usuarioId, jogoId);
        if (!possuiNaBiblioteca) {
            throw new AcessoNegadoException("O usuário não possui este jogo");
        }

        if (!jogo.possuiArquivo()) {
            throw new ArquivoNaoEncontradoException();
        }

        Resource resource = storageService.carregarArquivo(jogo.getArquivoStorageKey());

        return new DownloadFile(
            resource,
            jogo.getArquivoNomeOriginal(),
            jogo.getArquivoContentType(),
            Long.parseLong(jogo.getArquivoTamanhoBytes())
        );
    }
}