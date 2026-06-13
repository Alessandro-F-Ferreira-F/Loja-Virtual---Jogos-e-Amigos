package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.SolicitacaoSeguimento;
import dev.osdiscretos.atlantidastore.model.SolicitacaoSeguimento.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaSolicitacaoSeguimentoRepository extends JpaRepository<SolicitacaoSeguimento, UUID> {

    Optional<SolicitacaoSeguimento> findBySolicitanteIdAndAlvoId(UUID solicitanteId, UUID alvoId);

    List<SolicitacaoSeguimento> findByAlvoIdAndStatusOrderByCriadaEmDesc(UUID alvoId, Status status);

    boolean existsBySolicitanteIdAndAlvoIdAndStatus(UUID solicitanteId, UUID alvoId, Status status);

    List<SolicitacaoSeguimento> findByAlvoIdAndStatus(UUID alvoId, Status status);
}
