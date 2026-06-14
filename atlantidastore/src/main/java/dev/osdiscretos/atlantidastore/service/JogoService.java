package dev.osdiscretos.atlantidastore.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.osdiscretos.atlantidastore.dto.JogoResponseDTO;
import dev.osdiscretos.atlantidastore.dto.JogoResumoDTO;
import dev.osdiscretos.atlantidastore.dto.PublicarJogoRequestDTO;
import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.StatusJogo;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.JogoRepository;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;

@Service
public class JogoService {
    private final JogoRepository jogoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GameImageStorageService gameImageStorageService;

    public JogoService(
        JogoRepository jogoRepository,
        UsuarioRepository usuarioRepository,
        GameImageStorageService gameImageStorageService
    ) {
        this.jogoRepository = jogoRepository;
        this.usuarioRepository = usuarioRepository;
        this.gameImageStorageService = gameImageStorageService;
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

        Jogo jogo = Jogo.criarPublicado(
            nome,
            descricao,
            preco,
            tags,
            desenvolvedor
        );
        jogo.setImagemCapa(gameImageStorageService.salvarDataUrl(request.imagemCapa(), jogo.getId()));

        return JogoResponseDTO.from(jogoRepository.save(jogo));
    }

    @Transactional
    public List<JogoResumoDTO> listarFeed() {
        return jogoRepository.findByStatusOrderByDataPublicacaoDesc(StatusJogo.PUBLICADO).stream()
            .map(this::migrarCapaLegadaSeNecessario)
            .map(JogoResumoDTO::from)
            .toList();
    }

    @Transactional
    public JogoResponseDTO buscarDetalhes(UUID jogoId) {
        Jogo jogo = jogoRepository.findById(jogoId);

        if (jogo == null) {
            throw new NoSuchElementException("Jogo não encontrado");
        }

        return JogoResponseDTO.from(migrarCapaLegadaSeNecessario(jogo));
    }

    @Transactional
    public List<JogoResumoDTO> listarJogosPublicadosPorUsuario(UUID usuarioId) {
        return jogoRepository.findByDesenvolvedorIdOrderByDataPublicacaoDesc(usuarioId).stream()
            .map(this::migrarCapaLegadaSeNecessario)
            .map(JogoResumoDTO::from)
            .toList();
    }

    @Transactional
    public GameImageStorageService.CapaJogo buscarCapa(UUID jogoId) {
        Jogo jogo = jogoRepository.findById(jogoId);

        if (jogo == null) {
            throw new NoSuchElementException("Jogo não encontrado");
        }

        String imagemCapa = jogo.getImagemCapa();

        if (imagemCapa == null || imagemCapa.isBlank()) {
            throw new NoSuchElementException("Capa do jogo não encontrada");
        }

        Jogo jogoMigrado = migrarCapaLegadaSeNecessario(jogo);
        return gameImageStorageService.buscarPorUrl(jogoMigrado.getImagemCapa());
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private Jogo migrarCapaLegadaSeNecessario(Jogo jogo) {
        if (!gameImageStorageService.isDataUrl(jogo.getImagemCapa())) {
            return jogo;
        }

        try {
            jogo.setImagemCapa(gameImageStorageService.salvarDataUrl(jogo.getImagemCapa(), jogo.getId()));
        } catch (RuntimeException exception) {
            jogo.setImagemCapa(null);
        }

        return jogoRepository.save(jogo);
    }
}