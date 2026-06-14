package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaSessaoRepository extends JpaRepository<Sessao, String> {
    Optional<Sessao> findByToken(String token);
    void deleteByUsuarioId(UUID usuarioId);
    int deleteByExpiraEmBefore(java.time.LocalDateTime dataLimite);
}