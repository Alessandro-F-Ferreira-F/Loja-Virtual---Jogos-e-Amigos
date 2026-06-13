package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Seguimento;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class SeguimentoRepository {

    private final JpaSeguimentoRepository jpa;

    public SeguimentoRepository(JpaSeguimentoRepository jpa) {
        this.jpa = jpa;
    }

    public Seguimento save(Seguimento seguimento) {
        return jpa.save(seguimento);
    }

    public boolean existePar(UUID seguidorId, UUID seguidoId) {
        return jpa.existsBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    @Transactional
    public void remover(UUID seguidorId, UUID seguidoId) {
        jpa.deleteBySeguidorIdAndSeguidoId(seguidorId, seguidoId);
    }

    public List<Seguimento> seguidoresDe(UUID seguidoId) {
        return jpa.findBySeguidoId(seguidoId);
    }

    public List<Seguimento> seguindoDe(UUID seguidorId) {
        return jpa.findBySeguidorId(seguidorId);
    }
}
