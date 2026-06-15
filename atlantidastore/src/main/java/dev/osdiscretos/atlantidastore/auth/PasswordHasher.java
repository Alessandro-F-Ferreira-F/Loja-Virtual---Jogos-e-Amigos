package dev.osdiscretos.atlantidastore.auth;

import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
    private final PasswordHashStrategy strategy;

    public PasswordHasher(PasswordHashStrategy strategy) {
        this.strategy = strategy;
    }

    public String hash(String senha) {
        return strategy.hash(senha);
    }

    public boolean matches(String senhaDigitada, String senhaSalva) {
        return strategy.matches(senhaDigitada, senhaSalva);
    }
}