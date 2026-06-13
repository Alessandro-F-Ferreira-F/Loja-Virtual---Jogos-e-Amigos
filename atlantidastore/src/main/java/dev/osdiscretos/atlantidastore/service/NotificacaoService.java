package dev.osdiscretos.atlantidastore.service;

import dev.osdiscretos.atlantidastore.dto.NotificacaoResponse;
import dev.osdiscretos.atlantidastore.model.Notificacao;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.NotificacaoRepository;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(
        NotificacaoRepository notificacaoRepository,
        UsuarioRepository usuarioRepository
    ) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<NotificacaoResponse> listar(Usuario me) {
        return notificacaoRepository.listar(me.getId()).stream()
            .map(n -> NotificacaoResponse.from(n, buscarUsuario(n.getAtorId())))
            .toList();
    }

    public long contarNaoLidas(Usuario me) {
        return notificacaoRepository.contarNaoLidas(me.getId());
    }

    @Transactional
    public void marcarComoLida(Usuario me, UUID notificacaoId) {
        Notificacao notificacao = notificacaoRepository
            .findByIdEDestinatario(notificacaoId, me.getId())
            .orElseThrow(() -> new NoSuchElementException("Notificação não encontrada"));

        notificacao.marcarComoLida();
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void marcarTodasComoLidas(Usuario me) {
        List<Notificacao> pendentes = notificacaoRepository.listarNaoLidas(me.getId());
        pendentes.forEach(Notificacao::marcarComoLida);
        notificacaoRepository.saveAll(pendentes);
    }

    @Transactional
    public void criarNotificacao(UUID destinatarioId, UUID atorId, Notificacao.Tipo tipo, UUID referenciaId) {
        if (destinatarioId.equals(atorId)) return;

        Notificacao notificacao = new Notificacao(destinatarioId, atorId, tipo, referenciaId);
        notificacaoRepository.save(notificacao);
    }

    private Usuario buscarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findByID(id);
        if (usuario == null) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
        return usuario;
    }
}
