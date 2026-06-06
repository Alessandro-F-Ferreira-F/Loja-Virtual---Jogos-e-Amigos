package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Sessao;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class SessaoRepository {
    private final JpaSessaoRepository jpaRepository;

    public SessaoRepository(JpaSessaoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public Sessao save(Sessao sessao) {
        return jpaRepository.save(sessao);
    }

    public Sessao findByToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        var sessao = jpaRepository.findByToken(token).orElse(null);

        if (sessao != null && sessao.isExpirada()) {
            removeByToken(token);
            return null;
        }

        return sessao;
    }

    public void removeByToken(String token) {
        jpaRepository.deleteById(token);
    }

    public void removeByUsuarioId(UUID usuarioId) {
        jpaRepository.deleteByUsuarioId(usuarioId);
    }

    public void removeExpired() {
        jpaRepository.deleteByExpiraEmBefore(LocalDateTime.now());
    }
}
