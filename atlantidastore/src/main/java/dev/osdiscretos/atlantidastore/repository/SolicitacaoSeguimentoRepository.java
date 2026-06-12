package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.SolicitacaoSeguimento;
import dev.osdiscretos.atlantidastore.model.SolicitacaoSeguimento.Status;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SolicitacaoSeguimentoRepository {

    private final JpaSolicitacaoSeguimentoRepository jpa;

    public SolicitacaoSeguimentoRepository(JpaSolicitacaoSeguimentoRepository jpa) {
        this.jpa = jpa;
    }

    public SolicitacaoSeguimento save(SolicitacaoSeguimento solicitacao) {
        return jpa.save(solicitacao);
    }

    public Optional<SolicitacaoSeguimento> findById(UUID id) {
        return jpa.findById(id);
    }

    public Optional<SolicitacaoSeguimento> findPendente(UUID solicitanteId, UUID alvoId) {
        return jpa.findBySolicitanteIdAndAlvoId(solicitanteId, alvoId)
            .filter(SolicitacaoSeguimento::isPendente);
    }

    public boolean existePendente(UUID solicitanteId, UUID alvoId) {
        return jpa.existsBySolicitanteIdAndAlvoIdAndStatus(solicitanteId, alvoId, Status.PENDENTE);
    }

    public List<SolicitacaoSeguimento> listarPendentesRecebidas(UUID alvoId) {
        return jpa.findByAlvoIdAndStatusOrderByCriadaEmDesc(alvoId, Status.PENDENTE);
    }

    public List<SolicitacaoSeguimento> listarPendentesPorAlvo(UUID alvoId) {
        return jpa.findByAlvoIdAndStatus(alvoId, Status.PENDENTE);
    }
}
