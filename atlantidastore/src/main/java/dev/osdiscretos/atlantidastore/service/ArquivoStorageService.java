package dev.osdiscretos.atlantidastore.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import dev.osdiscretos.atlantidastore.dto.StoredFile;

import java.util.UUID;

public interface ArquivoStorageService {
    
    StoredFile salvarArquivo(UUID jogoId, MultipartFile arquivo);

    Resource carregarArquivo(String storageKey);

    void removerArquivo(String storageKey);

    boolean existeArquivo(String storageKey);
}
