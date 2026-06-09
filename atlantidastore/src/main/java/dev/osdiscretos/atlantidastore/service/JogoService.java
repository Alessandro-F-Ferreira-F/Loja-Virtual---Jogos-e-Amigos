package dev.osdiscretos.atlantidastore.service;

import dev.osdiscretos.atlantidastore.dto.JogoResponseDTO;
import dev.osdiscretos.atlantidastore.dto.JogoResumoDTO;
import dev.osdiscretos.atlantidastore.dto.PublicarJogoRequestDTO;
import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.StatusJogo;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.JogoRepository;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class JogoService {
    private final JogoRepository jogoRepository;
    private final UsuarioRepository usuarioRepository;

    public JogoService(JogoRepository jogoRepository, UsuarioRepository usuarioRepository) {
        this.jogoRepository = jogoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public JogoResponseDTO publicarJogo(UUID usuarioId, PublicarJogoRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados do jogo são obrigatórios");
        }

        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuário desenvolvedor é obrigatório");
        }

        String nome = normalize(request.nome());
        String descricao = normalize(request.descricao());
        BigDecimal preco = request.preco();
        String tags = normalize(request.tags());
        String imagemCapa = normalizeImagemCapa(request.imagemCapa());

        if (nome.isBlank()) {
            throw new IllegalArgumentException("Nome do jogo é obrigatório");
        }

        if (descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição do jogo é obrigatória");
        }

        if (preco == null) {
            throw new IllegalArgumentException("Preço do jogo é obrigatório");
        }

        if (preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço do jogo não pode ser negativo");
        }

        Usuario desenvolvedor = usuarioRepository.findByID(usuarioId);

        if (desenvolvedor == null) {
            throw new NoSuchElementException("Usuário desenvolvedor não encontrado");
        }

        Jogo jogo = new Jogo(
            nome,
            descricao,
            preco,
            tags,
            desenvolvedor,
            imagemCapa
        );

        return JogoResponseDTO.from(jogoRepository.save(jogo));
    }

    @Transactional(readOnly = true)
    public List<JogoResumoDTO> listarFeed() {
        return jogoRepository.findByStatusOrderByDataPublicacaoDesc(StatusJogo.PUBLICADO).stream()
            .map(JogoResumoDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public JogoResponseDTO buscarDetalhes(UUID jogoId) {
        Jogo jogo = jogoRepository.findById(jogoId);

        if (jogo == null) {
            throw new NoSuchElementException("Jogo não encontrado");
        }

        return JogoResponseDTO.from(jogo);
    }

    @Transactional(readOnly = true)
    public List<JogoResumoDTO> listarJogosPublicadosPorUsuario(UUID usuarioId) {
        return jogoRepository.findByDesenvolvedorIdOrderByDataPublicacaoDesc(usuarioId).stream()
            .map(JogoResumoDTO::from)
            .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeImagemCapa(String value) {
        String normalized = normalize(value);
        return normalized.isBlank() ? null : normalized;
    }
}
