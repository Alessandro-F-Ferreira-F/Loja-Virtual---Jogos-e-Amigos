package dev.osdiscretos.atlantidastore.service;

import dev.osdiscretos.atlantidastore.dto.RelacionamentoResponse;
import dev.osdiscretos.atlantidastore.dto.SeguimentoResponse;
import dev.osdiscretos.atlantidastore.dto.SolicitacaoSeguimentoResponse;
import dev.osdiscretos.atlantidastore.model.Seguimento;
import dev.osdiscretos.atlantidastore.model.SolicitacaoSeguimento;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.SeguimentoRepository;
import dev.osdiscretos.atlantidastore.repository.SolicitacaoSeguimentoRepository;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SocialService {

    private final SeguimentoRepository seguimentoRepository;
    private final SolicitacaoSeguimentoRepository solicitacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public SocialService(
        SeguimentoRepository seguimentoRepository,
        SolicitacaoSeguimentoRepository solicitacaoRepository,
        UsuarioRepository usuarioRepository
    ) {
        this.seguimentoRepository = seguimentoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public RelacionamentoResponse seguir(Usuario me, UUID alvoId) {
        if (me.getId().equals(alvoId)) {
            throw new IllegalArgumentException("Você não pode seguir a si mesmo");
        }

        Usuario alvo = buscarUsuario(alvoId);

        if (seguimentoRepository.existePar(me.getId(), alvoId)) {
            throw new IllegalStateException("Você já segue este usuário");
        }

        if (solicitacaoRepository.existePendente(me.getId(), alvoId)) {
            throw new IllegalStateException("Já existe uma solicitação pendente para este usuário");
        }

        if (alvo.isPerfilPrivado()) {
            SolicitacaoSeguimento solicitacao = new SolicitacaoSeguimento(me.getId(), alvoId);
            solicitacaoRepository.save(solicitacao);
            return RelacionamentoResponse.pendente();
        }

        Seguimento seguimento = new Seguimento(me.getId(), alvoId);
        seguimentoRepository.save(seguimento);
        return RelacionamentoResponse.seguindo();
    }

    @Transactional
    public void deixarDeSeguir(Usuario me, UUID alvoId) {
        buscarUsuario(alvoId);

        if (!seguimentoRepository.existePar(me.getId(), alvoId)) {
            throw new IllegalArgumentException("Você não segue este usuário");
        }

        seguimentoRepository.remover(me.getId(), alvoId);
    }

    @Transactional
    public void cancelarSolicitacao(Usuario me, UUID alvoId) {
        buscarUsuario(alvoId);

        SolicitacaoSeguimento solicitacao = solicitacaoRepository
            .findPendente(me.getId(), alvoId)
            .orElseThrow(() -> new NoSuchElementException("Solicitação pendente não encontrada"));

        solicitacao.anular();
        solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void aceitarSolicitacao(Usuario me, UUID solicitacaoId) {
        SolicitacaoSeguimento solicitacao = buscarSolicitacaoPendente(solicitacaoId, me.getId());

        solicitacao.aceitar();
        solicitacaoRepository.save(solicitacao);

        Seguimento seguimento = new Seguimento(solicitacao.getSolicitanteId(), me.getId());
        seguimentoRepository.save(seguimento);
    }

    @Transactional
    public void recusarSolicitacao(Usuario me, UUID solicitacaoId) {
        SolicitacaoSeguimento solicitacao = buscarSolicitacaoPendente(solicitacaoId, me.getId());
        solicitacao.recusar();
        solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public void alterarPrivacidade(Usuario me, boolean tornarPrivado) {
        if (tornarPrivado) {
            me.tornarPrivado();
        } else {
            solicitacaoRepository.listarPendentesRecebidas(me.getId())
                .forEach(s -> {
                    s.anular();
                    solicitacaoRepository.save(s);
                });
            me.tornarPublico();
        }
        usuarioRepository.save(me);
    }

    public List<SeguimentoResponse> listarSeguidores(UUID alvoId) {
        buscarUsuario(alvoId);
        return seguimentoRepository.seguidoresDe(alvoId).stream()
            .map(s -> SeguimentoResponse.from(buscarUsuario(s.getSeguidorId())))
            .toList();
    }

    public List<SeguimentoResponse> listarSeguindo(UUID usuarioId) {
        buscarUsuario(usuarioId);
        return seguimentoRepository.seguindoDe(usuarioId).stream()
            .map(s -> SeguimentoResponse.from(buscarUsuario(s.getSeguidoId())))
            .toList();
    }

    public RelacionamentoResponse statusRelacionamento(Usuario me, UUID alvoId) {
        buscarUsuario(alvoId);

        if (seguimentoRepository.existePar(me.getId(), alvoId)) {
            return RelacionamentoResponse.seguindo();
        }
        if (solicitacaoRepository.existePendente(me.getId(), alvoId)) {
            return RelacionamentoResponse.pendente();
        }
        return RelacionamentoResponse.nenhum();
    }

    public List<SolicitacaoSeguimentoResponse> listarSolicitacoesPendentes(Usuario me) {
        return solicitacaoRepository.listarPendentesRecebidas(me.getId()).stream()
            .map(s -> SolicitacaoSeguimentoResponse.from(s, buscarUsuario(s.getSolicitanteId())))
            .toList();
    }

    private Usuario buscarUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findByID(id);
        if (usuario == null) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
        return usuario;
    }

    private SolicitacaoSeguimento buscarSolicitacaoPendente(UUID solicitacaoId, UUID alvoEsperado) {
        SolicitacaoSeguimento solicitacao = solicitacaoRepository.findById(solicitacaoId)
            .orElseThrow(() -> new NoSuchElementException("Solicitação não encontrada"));

        if (!solicitacao.getAlvoId().equals(alvoEsperado)) {
            throw new IllegalArgumentException("Você não tem permissão para responder esta solicitação");
        }

        if (!solicitacao.isPendente()) {
            throw new IllegalStateException("Esta solicitação já foi resolvida");
        }

        return solicitacao;
    }
}
