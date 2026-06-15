package dev.osdiscretos.atlantidastore.service;

import dev.osdiscretos.atlantidastore.dto.JogoResumoDTO;
import dev.osdiscretos.atlantidastore.model.BibliotecaItem;
import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.StatusJogo;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.BibliotecaRepository;
import dev.osdiscretos.atlantidastore.repository.JogoRepository;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BibliotecaService {
    private final BibliotecaRepository bibliotecaRepository;
    private final UsuarioRepository usuarioRepository;
    private final JogoRepository jogoRepository;

    public BibliotecaService(
        BibliotecaRepository bibliotecaRepository,
        UsuarioRepository usuarioRepository,
        JogoRepository jogoRepository
    ) {
        this.bibliotecaRepository = bibliotecaRepository;
        this.usuarioRepository = usuarioRepository;
        this.jogoRepository = jogoRepository;
    }

    @Transactional(readOnly = true)
    public List<JogoResumoDTO> listarBiblioteca(UUID usuarioId) {
        return bibliotecaRepository.findByUsuarioId(usuarioId).stream()
            .map(BibliotecaItem::getJogo)
            .filter(jogo -> jogo.getStatus() == StatusJogo.PUBLICADO)
            .map(JogoResumoDTO::from)
            .toList();
    }

    @Transactional
    public void adicionarJogo(UUID usuarioId, UUID jogoId) {
        Usuario usuario = usuarioRepository.findByID(usuarioId);

        if (usuario == null) {
            throw new NoSuchElementException("Usuário não encontrado");
        }

        Jogo jogo = jogoRepository.findById(jogoId);

        if (jogo == null) {
            throw new NoSuchElementException("Jogo não encontrado");
        }

        if (jogo.getStatus() != StatusJogo.PUBLICADO) {
            throw new IllegalArgumentException("Apenas jogos publicados podem ser adicionados à biblioteca");
        }

        if (bibliotecaRepository.existsByUsuarioIdAndJogoId(usuarioId, jogoId)) {
            throw new IllegalArgumentException("Jogo já está na biblioteca");
        }

        bibliotecaRepository.save(new BibliotecaItem(usuario, jogo));
    }

    @Transactional
    public void removerJogo(UUID usuarioId, UUID jogoId) {
        BibliotecaItem item = bibliotecaRepository.findByUsuarioIdAndJogoId(usuarioId, jogoId);

        if (item != null) {
            bibliotecaRepository.deleteByUsuarioIdAndJogoId(usuarioId, jogoId);
        }
    }
}
