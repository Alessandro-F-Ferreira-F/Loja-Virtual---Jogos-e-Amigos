package dev.osdiscretos.atlantidastore.service;


import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.dto.CadastrarUsuarioRequestDTO;
import dev.osdiscretos.atlantidastore.dto.UsuarioResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class UsuarioService {
    private final List<Usuario> usuarios = new CopyOnWriteArrayList<>();

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

        boolean emailJaExiste = usuarios.stream()
            .anyMatch(usuario -> usuario.getEmail().equalsIgnoreCase(emailNormalizado));

        if (emailJaExiste) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        Usuario usuario = new Usuario(nomeNormalizado, emailNormalizado);
        usuarios.add(usuario);

        return UsuarioResponse.from(usuario);
    }

    public List<UsuarioResponse> listar() {
        return usuarios.stream()
            .map(UsuarioResponse::from)
            .toList();
    }

    public void remover(UUID id) {
        boolean removeu = usuarios.removeIf(usuario -> usuario.getId().equals(id));

        if (!removeu) {
            throw new NoSuchElementException("Usuário não encontrado");
        }
    }
}
