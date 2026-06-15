package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.ListaDesejosItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListaDesejosRepository extends JpaRepository<ListaDesejosItem, UUID> {
    List<ListaDesejosItem> findByUsuarioIdOrderByDataAdicaoDesc(UUID usuarioId);
    ListaDesejosItem findByUsuarioIdAndJogoId(UUID usuarioId, UUID jogoId);
    boolean existsByUsuarioIdAndJogoId(UUID usuarioId, UUID jogoId);
    void deleteByUsuario_Id(UUID usuarioId);
    void deleteByJogo_Id(UUID jogoId);
}
