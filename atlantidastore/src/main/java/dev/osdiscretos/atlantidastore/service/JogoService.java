package dev.osdiscretos.atlantidastore.service;

import dev.osdiscretos.atlantidastore.dto.CadastrarJogoRequestDTO;
import dev.osdiscretos.atlantidastore.dto.JogoResponse;
import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.JogoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class JogoService {
    private final JogoRepository jogoRepository;

    public JogoService(JogoRepository jogoRepository) {
        this.jogoRepository = jogoRepository;
    }

    public JogoResponse cadastrar(CadastrarJogoRequestDTO request, Usuario publicador) {
        if (request == null) {
            throw new IllegalArgumentException("Dados do jogo são obrigatórios");
        }

        if (publicador == null) {
            throw new IllegalArgumentException("Usuário publicador é obrigatório");
        }

        String titulo = normalize(request.titulo());
        String descricao = normalize(request.descricao());
        BigDecimal preco = request.preco();
        List<String> categorias = normalizeCategorias(request.categorias());
        String downloadUrl = normalize(request.downloadUrl());
        String imagemCapa = normalize(request.imagemCapa());

        if (titulo.isBlank()) {
            throw new IllegalArgumentException("Título do jogo é obrigatório");
        }

        if (preco == null) {
            throw new IllegalArgumentException("Preço do jogo é obrigatório");
        }

        if (preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço do jogo não pode ser negativo");
        }

        if (jogoRepository.existsByTituloIgnoreCase(titulo)) {
            throw new IllegalArgumentException("Já existe um jogo cadastrado com este título");
        }

        Jogo jogo = new Jogo(
            titulo,
            descricao,
            preco,
            publicador.getId(),
            categorias,
            downloadUrl,
            imagemCapa
        );

        return JogoResponse.from(jogoRepository.save(jogo));
    }

    public List<JogoResponse> listar() {
        return jogoRepository.findAll().stream()
            .map(JogoResponse::from)
            .toList();
    }

    public void remover(UUID id) {
        if (jogoRepository.findById(id) == null) {
            throw new NoSuchElementException("Jogo não encontrado");
        }

        jogoRepository.deleteById(id);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private List<String> normalizeCategorias(List<String> categorias) {
        if (categorias == null) {
            return List.of();
        }

        return categorias.stream()
            .map(this::normalize)
            .filter(categoria -> !categoria.isBlank())
            .distinct()
            .toList();
    }
}
