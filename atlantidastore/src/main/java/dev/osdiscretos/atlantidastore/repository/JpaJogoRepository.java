package dev.osdiscretos.atlantidastore.repository;
import dev.osdiscretos.atlantidastore.model.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaJogoRepository extends JpaRepository<Jogo, UUID> {
    boolean existsByTituloIgnoreCase(String titulo);
    List<Jogo> findAllByOrderByDataCriacaoAsc();
}
