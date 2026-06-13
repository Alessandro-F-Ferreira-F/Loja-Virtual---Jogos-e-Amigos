package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Seguimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaSeguimentoRepository extends JpaRepository<Seguimento, UUID> {

    List<Seguimento> findBySeguidoId(UUID seguidoId);

    List<Seguimento> findBySeguidorId(UUID seguidorId);

    boolean existsBySeguidorIdAndSeguidoId(UUID seguidorId, UUID seguidoId);

    void deleteBySeguidorIdAndSeguidoId(UUID seguidorId, UUID seguidoId);
}
