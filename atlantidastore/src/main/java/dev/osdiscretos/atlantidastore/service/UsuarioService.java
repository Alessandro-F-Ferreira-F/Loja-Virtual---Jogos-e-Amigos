package dev.osdiscretos.atlantidastore.service;


import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.dto.CadastrarUsuarioRequestDTO;
import dev.osdiscretos.atlantidastore.dto.UsuarioResponse;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse cadastrar(CadastrarUsuarioRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados do usuário são obrigatórios");
        }

        String nome = request.nome();
        String email = request.email();

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }

        String nomeNormalizado = nome.trim();
        String emailNormalizado = email.trim();

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        Usuario usuario = new Usuario(nomeNormalizado, emailNormalizado);
        usuario = usuarioRepository.save(usuario);

        return UsuarioResponse.from(usuario);
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
            .map(UsuarioResponse::from)
            .toList();
    }

    public void remover(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
