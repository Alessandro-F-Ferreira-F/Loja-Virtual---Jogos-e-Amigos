package dev.osdiscretos.atlantidastore.service;


import dev.osdiscretos.atlantidastore.auth.PasswordHasher;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.SessaoRepository;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;
import dev.osdiscretos.atlantidastore.dto.CadastroRequestDTO;
import dev.osdiscretos.atlantidastore.dto.UsuarioResponse;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final SessaoRepository sessaoRepository;
    private final PasswordHasher passwordHasher;

    public UsuarioService(
        UsuarioRepository usuarioRepository,
        SessaoRepository sessaoRepository,
        PasswordHasher passwordHasher
    ) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
        this.passwordHasher = passwordHasher;
    }


    public UsuarioResponse register(CadastroRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados do usuário são obrigatórios");
        }

        String nome = normalize(request.nome());
        String email = normalize(request.email()).toLowerCase();
        String senha = request.senha();

        if (nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }

        if (email.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("E-mail inválido");
        }

        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }

        if (usuarioRepository.isEmailRegistered(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        String senhaHash = passwordHasher.hash(senha);

        Usuario user = new Usuario(
            nome,
            email,
            senhaHash
        );

        Usuario saved = usuarioRepository.save(user);
        return UsuarioResponse.from(saved);
    }

    public List<UsuarioResponse> listAll() {
        List<Usuario> users = usuarioRepository.listAll();

        List<UsuarioResponse> convertedList = new ArrayList<>();

        for (Usuario user : users) {
            UsuarioResponse response = UsuarioResponse.from(user);
            convertedList.add(response);
        }
        
        return convertedList;
    }

    public void remove(UUID id) {
        Usuario userToDelete = usuarioRepository.findByID(id);
        if (userToDelete == null) {
            throw new NoSuchElementException("Usuario não encontrado ");
        }

        usuarioRepository.removeByID(id);
        sessaoRepository.removeByUsuarioId(id);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
