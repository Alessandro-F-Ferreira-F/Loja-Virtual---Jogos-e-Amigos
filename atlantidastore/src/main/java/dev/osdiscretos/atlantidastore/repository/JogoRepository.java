package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Jogo;
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

    public List<Jogo> findAll() {
        return jpaRepository.findAllByOrderByDataCriacaoAsc();
    }

    public boolean existsByTituloIgnoreCase(String titulo) {
        if (titulo == null) {
            return false;
        }
        return jpaRepository.existsByTituloIgnoreCase(titulo);
    }

    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
