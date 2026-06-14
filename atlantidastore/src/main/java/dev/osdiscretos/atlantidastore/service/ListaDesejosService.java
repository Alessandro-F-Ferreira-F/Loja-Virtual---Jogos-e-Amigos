package dev.osdiscretos.atlantidastore.service;

import dev.osdiscretos.atlantidastore.dto.JogoResumoDTO;
import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.ListaDesejosItem;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.JogoRepository;
import dev.osdiscretos.atlantidastore.repository.ListaDesejosRepository;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ListaDesejosService {
    private final ListaDesejosRepository listaDesejosRepository;
    private final UsuarioRepository usuarioRepository;
    private final JogoRepository jogoRepository;

    public ListaDesejosService(
            ListaDesejosRepository listaDesejosRepository,
            UsuarioRepository usuarioRepository,
            JogoRepository jogoRepository) {
        this.listaDesejosRepository = listaDesejosRepository;
        this.usuarioRepository = usuarioRepository;
        this.jogoRepository = jogoRepository;
    }

    @Transactional
    public void adicionarJogo(UUID usuarioId, UUID jogoId) {
        if (listaDesejosRepository.existsByUsuarioIdAndJogoId(usuarioId, jogoId)) {
            throw new IllegalArgumentException("Este jogo já está na sua lista de desejos.");
        }

        Usuario usuario = usuarioRepository.findByID(usuarioId);
        if (usuario == null) {
            throw new NoSuchElementException("Usuário não encontrado.");
        }

        Jogo jogo = jogoRepository.findById(jogoId);
        if (jogo == null) {
            throw new NoSuchElementException("Jogo não encontrado.");
        }

        ListaDesejosItem item = new ListaDesejosItem(usuario, jogo);
        listaDesejosRepository.save(item);
    }

    @Transactional
    public void removerJogo(UUID usuarioId, UUID jogoId) {
        ListaDesejosItem item = listaDesejosRepository.findByUsuarioIdAndJogoId(usuarioId, jogoId);
        if (item != null) {
            listaDesejosRepository.delete(item);
        }
    }

    @Transactional
    public List<JogoResumoDTO> listarJogosDesejados(UUID usuarioId) {
        return listaDesejosRepository.findByUsuarioIdOrderByDataAdicaoDesc(usuarioId).stream()
                .map(item -> JogoResumoDTO.from(item.getJogo())).toList();
    }
}