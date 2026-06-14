package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.BibliotecaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaBibliotecaRepository extends JpaRepository<BibliotecaItem, UUID> {
    List<BibliotecaItem> findByUsuario_IdOrderByDataAdicaoDesc(UUID usuarioId);
    Optional<BibliotecaItem> findByUsuario_IdAndJogo_Id(UUID usuarioId, UUID jogoId);
    boolean existsByUsuario_IdAndJogo_Id(UUID usuarioId, UUID jogoId);
    void deleteByUsuario_IdAndJogo_Id(UUID usuarioId, UUID jogoId);
}
