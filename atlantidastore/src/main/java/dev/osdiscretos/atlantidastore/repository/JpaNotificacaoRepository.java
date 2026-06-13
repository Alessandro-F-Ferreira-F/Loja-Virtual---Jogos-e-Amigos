package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaNotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    List<Notificacao> findByDestinatarioIdOrderByLidaAscCriadaEmDesc(UUID destinatarioId);

    long countByDestinatarioIdAndLidaFalse(UUID destinatarioId);

    Optional<Notificacao> findByIdAndDestinatarioId(UUID id, UUID destinatarioId);

    List<Notificacao> findByDestinatarioIdAndLidaFalse(UUID destinatarioId);
}
