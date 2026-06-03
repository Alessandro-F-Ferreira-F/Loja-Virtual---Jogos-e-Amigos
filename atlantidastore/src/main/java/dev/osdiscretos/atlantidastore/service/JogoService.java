package dev.osdiscretos.atlantidastore.service;

import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.dto.CadastrarJogoRequestDTO;
import dev.osdiscretos.atlantidastore.dto.JogoResponse;
import dev.osdiscretos.atlantidastore.repository.JogoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class JogoService {
    private final JogoRepository jogoRepository;

    public JogoService(JogoRepository jogoRepository) {
        this.jogoRepository = jogoRepository;
    }

    public JogoResponse cadastrar(CadastrarJogoRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados do jogo são obrigatórios");
        }

        String nome = request.nome();
        String descricao = request.descricao();
        double preco = request.preco();

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do jogo é obrigatório");
        }

        if (preco < 0) {
            throw new IllegalArgumentException("O preço não pode ser negativo");
        }

        String nomeNormalizado = nome.trim();
        String descricaoNormalizada = descricao != null ? descricao.trim() : "";

        if (jogoRepository.existsByNomeIgnoreCase(nomeNormalizado)) {
            throw new IllegalArgumentException("Já existe um jogo cadastrado com este nome");
        }

        Jogo jogo = new Jogo(nomeNormalizado, descricaoNormalizada, preco);
        jogo = jogoRepository.save(jogo);

        return JogoResponse.from(jogo);
    }

    public List<JogoResponse> listar() {
        return jogoRepository.findAll().stream()
            .map(JogoResponse::from)
            .toList();
    }

    public void remover(UUID id) {
        if (!jogoRepository.existsById(id)) {
            throw new NoSuchElementException("Jogo não encontrado");
        }
        jogoRepository.deleteById(id);
    }
}
