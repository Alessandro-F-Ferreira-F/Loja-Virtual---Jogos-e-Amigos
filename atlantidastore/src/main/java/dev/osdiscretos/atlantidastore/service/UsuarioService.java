package dev.osdiscretos.atlantidastore.service;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.osdiscretos.atlantidastore.auth.PasswordHasher;
import dev.osdiscretos.atlantidastore.dto.CadastroRequestDTO;
import dev.osdiscretos.atlantidastore.dto.PerfilPublicoUsuarioDTO;
import dev.osdiscretos.atlantidastore.dto.PerfilUsuarioDTO;
import dev.osdiscretos.atlantidastore.dto.UsuarioResponse;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.SessaoRepository;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final SessaoRepository sessaoRepository;
    private final PasswordHasher passwordHasher;
    private final JogoService jogoService;
    private final BibliotecaService bibliotecaService;

    public UsuarioService(
        UsuarioRepository usuarioRepository,
        SessaoRepository sessaoRepository,
        PasswordHasher passwordHasher,
        JogoService jogoService,
        BibliotecaService bibliotecaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
        this.passwordHasher = passwordHasher;
        this.jogoService = jogoService;
        this.bibliotecaService = bibliotecaService;
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

        Usuario user = Usuario.cadastrar(
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

    @Transactional(readOnly = true)
    public PerfilUsuarioDTO perfil(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findByID(usuarioId);

        if (usuario == null) {
            throw new NoSuchElementException("Usuário não encontrado");
        }

        return PerfilUsuarioDTO.from(
            usuario,
            jogoService.listarJogosPublicadosPorUsuario(usuarioId),
            bibliotecaService.listarBiblioteca(usuarioId)
        );
    }

    @Transactional(readOnly = true)
    public PerfilPublicoUsuarioDTO perfilPublico(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findByID(usuarioId);

        if (usuario == null) {
            throw new NoSuchElementException("Usuário não encontrado");
        }

        return PerfilPublicoUsuarioDTO.from(
            usuario,
            jogoService.listarJogosPublicadosPorUsuario(usuarioId)
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}