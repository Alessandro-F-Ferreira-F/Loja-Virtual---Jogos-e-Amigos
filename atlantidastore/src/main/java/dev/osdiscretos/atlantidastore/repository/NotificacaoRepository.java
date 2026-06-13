package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Notificacao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificacaoRepository {

    private final JpaNotificacaoRepository jpa;

    public NotificacaoRepository(JpaNotificacaoRepository jpa) {
        this.jpa = jpa;
    }

    public Notificacao save(Notificacao notificacao) {
        return jpa.save(notificacao);
    }

    public void saveAll(List<Notificacao> notificacoes) {
        jpa.saveAll(notificacoes);
    }

    public List<Notificacao> listar(UUID destinatarioId) {
        return jpa.findByDestinatarioIdOrderByLidaAscCriadaEmDesc(destinatarioId);
    }

    public long contarNaoLidas(UUID destinatarioId) {
        return jpa.countByDestinatarioIdAndLidaFalse(destinatarioId);
    }

    public Optional<Notificacao> findByIdEDestinatario(UUID id, UUID destinatarioId) {
        return jpa.findByIdAndDestinatarioId(id, destinatarioId);
    }

    public List<Notificacao> listarNaoLidas(UUID destinatarioId) {
        return jpa.findByDestinatarioIdAndLidaFalse(destinatarioId);
    }
}
