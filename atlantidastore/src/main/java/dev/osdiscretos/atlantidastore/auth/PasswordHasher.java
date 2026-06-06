package dev.osdiscretos.atlantidastore.auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(senha, salt, ITERATIONS);

        return "pbkdf2$" + ITERATIONS + "$" + encode(salt) + "$" + encode(hash);
    }

    public boolean matches(String senhaDigitada, String senhaSalva) {
        if (senhaDigitada == null || senhaSalva == null || senhaSalva.isBlank()) {
            return false;
        }

        String[] partes = senhaSalva.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
            return false;
        }

        int iterations = Integer.parseInt(partes[1]);
        byte[] salt = decode(partes[2]);
        byte[] expectedHash = decode(partes[3]);
        byte[] actualHash = pbkdf2(senhaDigitada, salt, iterations);

        return MessageDigest.isEqual(expectedHash, actualHash);
    }

    private byte[] pbkdf2(String senha, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iterations, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Não foi possível gerar hash da senha", exception);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private byte[] decode(String value) {
        return Base64.getDecoder().decode(value);
    }
}
