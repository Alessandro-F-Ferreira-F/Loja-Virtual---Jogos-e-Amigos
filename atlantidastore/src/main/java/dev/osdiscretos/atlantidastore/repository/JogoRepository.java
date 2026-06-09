package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.StatusJogo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class JogoRepository {
    private final JpaJogoRepository jpaRepository;

    public JogoRepository(JpaJogoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public Jogo save(Jogo jogo) {
        return jpaRepository.save(jogo);
    }

    public Jogo findById(UUID id) {
        return jpaRepository.findById(id).orElse(null);
    }

    public List<Jogo> findByStatusOrderByDataPublicacaoDesc(StatusJogo status) {
        return jpaRepository.findByStatusOrderByDataPublicacaoDesc(status);
    }

    public List<Jogo> findByDesenvolvedorIdOrderByDataPublicacaoDesc(UUID desenvolvedorId) {
        return jpaRepository.findByDesenvolvedor_IdOrderByDataPublicacaoDesc(desenvolvedorId);
    }

    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
