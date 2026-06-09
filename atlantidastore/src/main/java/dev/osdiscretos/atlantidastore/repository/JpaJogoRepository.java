package dev.osdiscretos.atlantidastore.repository;
import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.StatusJogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaJogoRepository extends JpaRepository<Jogo, UUID> {
    @Query("""
        select jogo
        from Jogo jogo
        join fetch jogo.desenvolvedor
        where jogo.status = :status
        order by jogo.dataPublicacao desc
        """)
    List<Jogo> findByStatusOrderByDataPublicacaoDesc(StatusJogo status);

    @Query("""
        select jogo
        from Jogo jogo
        join fetch jogo.desenvolvedor
        where jogo.desenvolvedor.id = :desenvolvedorId
        order by jogo.dataPublicacao desc
        """)
    List<Jogo> findByDesenvolvedor_IdOrderByDataPublicacaoDesc(UUID desenvolvedorId);
}
