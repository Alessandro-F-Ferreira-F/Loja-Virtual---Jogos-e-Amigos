package dev.osdiscretos.atlantidastore.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

import dev.osdiscretos.atlantidastore.auth.PasswordHasher;
import dev.osdiscretos.atlantidastore.dto.LoginRequest;
import dev.osdiscretos.atlantidastore.model.Sessao;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.SessaoRepository;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;

@Service
public class AuthService {
    private static final int SESSION_MAX_AGE_SECONDS = 30 * 60;
    private static final int SESSION_TOKEN_BYTES = 32;

    private final UsuarioRepository usuarioRepository;
    private final SessaoRepository sessaoRepository;
    private final PasswordHasher passwordHasher;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
        UsuarioRepository usuarioRepository,
        SessaoRepository sessaoRepository,
        PasswordHasher passwordHasher
    ) {
        this.usuarioRepository = usuarioRepository;
        this.sessaoRepository = sessaoRepository;
        this.passwordHasher = passwordHasher;
    }

    public LoginResult login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Dados de login são obrigatórios");
        }

        Usuario user = authenticate(request.email(), request.senha());
        Sessao sessao = Sessao.criarParaUsuario(
            gerarToken(),
            user.getId(),
            SESSION_MAX_AGE_SECONDS
        );

        sessaoRepository.removeExpired();
        sessaoRepository.save(sessao);

        return new LoginResult(user, sessao);
    }

    public Usuario authenticate(String email, String senhaDigitada) {
        Usuario user = usuarioRepository.findByEmail(email == null ? "" : email.trim());

        if (user == null) {
            throw new IllegalArgumentException("E-mail ou senha inválidos");
        }

        boolean senhaValida = passwordHasher.matches(senhaDigitada, user.getSenhaHash());
        if (!senhaValida) {
            throw new IllegalArgumentException("E-mail ou senha inválidos");
        }

        return user;
    }

    public Usuario findUserBySessionToken(String token) {
        Sessao sessao = sessaoRepository.findByToken(token);

        if (sessao == null) {
            return null;
        }

        Usuario usuario = usuarioRepository.findByID(sessao.getUsuarioId());

        if (usuario == null) {
            sessaoRepository.removeByToken(token);
        }

        return usuario;
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            sessaoRepository.removeByToken(token);
        }
    }

    public int sessionMaxAgeSeconds() {
        return SESSION_MAX_AGE_SECONDS;
    }

    private String gerarToken() {
        byte[] bytes = new byte[SESSION_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record LoginResult(Usuario usuario, Sessao sessao) {
    }
}
