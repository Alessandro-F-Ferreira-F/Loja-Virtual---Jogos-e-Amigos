package dev.osdiscretos.atlantidastore.auth;

public interface PasswordHashStrategy {
    String hash(String senha);

    boolean matches(String senhaDigitada, String senhaSalva);
}