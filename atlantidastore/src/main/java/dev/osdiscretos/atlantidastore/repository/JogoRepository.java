package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JogoRepository extends JpaRepository<Jogo, UUID> {
    boolean existsByNomeIgnoreCase(String nome);
}
