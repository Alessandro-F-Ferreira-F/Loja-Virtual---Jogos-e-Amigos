package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.BibliotecaItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class BibliotecaRepository {
    private final JpaBibliotecaRepository jpaRepository;

    public BibliotecaRepository(JpaBibliotecaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public BibliotecaItem save(BibliotecaItem item) {
        return jpaRepository.save(item);
    }

    public List<BibliotecaItem> findByUsuarioId(UUID usuarioId) {
        return jpaRepository.findByUsuario_IdOrderByDataAdicaoDesc(usuarioId);
    }

    public BibliotecaItem findByUsuarioIdAndJogoId(UUID usuarioId, UUID jogoId) {
        return jpaRepository.findByUsuario_IdAndJogo_Id(usuarioId, jogoId).orElse(null);
    }

    public boolean existsByUsuarioIdAndJogoId(UUID usuarioId, UUID jogoId) {
        return jpaRepository.existsByUsuario_IdAndJogo_Id(usuarioId, jogoId);
    }

    public void deleteByUsuarioIdAndJogoId(UUID usuarioId, UUID jogoId) {
        jpaRepository.deleteByUsuario_IdAndJogo_Id(usuarioId, jogoId);
    }
}
