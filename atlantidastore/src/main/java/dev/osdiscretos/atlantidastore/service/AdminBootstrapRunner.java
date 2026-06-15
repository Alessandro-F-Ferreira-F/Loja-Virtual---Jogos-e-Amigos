package dev.osdiscretos.atlantidastore.service;

import dev.osdiscretos.atlantidastore.auth.PasswordHasher;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final boolean enabled;
    private final String nome;
    private final String email;
    private final String senha;

    public AdminBootstrapRunner(
        UsuarioRepository usuarioRepository,
        PasswordHasher passwordHasher,
        @Value("${app.admin.enabled:true}") boolean enabled,
        @Value("${app.admin.nome:Administrador}") String nome,
        @Value("${app.admin.email:admin@atlantida.local}") String email,
        @Value("${app.admin.password:admin123}") String senha
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.enabled = enabled;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        String emailNormalizado = normalize(email).toLowerCase();
        Usuario existente = usuarioRepository.findByEmail(emailNormalizado);

        if (existente != null) {
            boolean alterado = false;

            if (!existente.isAdministrador()) {
                existente.tornarAdministrador();
                alterado = true;
            }

            if (!passwordHasher.matches(senha, existente.getSenhaHash())) {
                existente.alterarSenhaHash(passwordHasher.hash(senha));
                alterado = true;
            }

            if (alterado) {
                usuarioRepository.save(existente);
            }

            return;
        }

        Usuario admin = new Usuario(
            normalize(nome).isBlank() ? "Administrador" : normalize(nome),
            emailNormalizado,
            passwordHasher.hash(senha)
        );
        admin.tornarAdministrador();
        usuarioRepository.save(admin);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
