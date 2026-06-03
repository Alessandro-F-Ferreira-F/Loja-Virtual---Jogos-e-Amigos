package dev.osdiscretos.atlantidastore.repository;


import dev.osdiscretos.atlantidastore.model.Usuario;

import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Repository
public class UsuarioRepositoryImp implements UsuarioRepository {
    private static final String HEADER = "id,nome,email,senhaHash,dataCriacao";

    private final Path arquivo = Path.of("data", "usuarios.csv");

    @Override
    public Usuario save(Usuario user) {
        synchronized (this) {
            List<Usuario> users = new ArrayList<>(readAll());
            users.removeIf(existingUser -> existingUser.getId().equals(user.getId()));
            users.add(user);
            writeAll(users);
            return user;
        }
    }

    @Override
    public Usuario findByID(UUID id) {
        return readAll().stream()
            .filter(user -> user.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Usuario findByEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalizedEmail = email.trim();

        return readAll().stream()
            .filter(user -> user.getEmail().equalsIgnoreCase(normalizedEmail))
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<Usuario> listAll() {
        return readAll().stream()
            .sorted(Comparator.comparing(Usuario::getDataCriacao))
            .toList();
    }

    @Override
    public void removeByID(UUID id) {
        synchronized (this) {
            List<Usuario> users = new ArrayList<>(readAll());
            users.removeIf(user -> user.getId().equals(id));
            writeAll(users);
        }
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return findByEmail(email) != null;
    }

    private List<Usuario> readAll() {
        ensureFileExists();

        try {
            return Files.readAllLines(arquivo, StandardCharsets.UTF_8).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(this::fromCsv)
                .filter(Objects::nonNull)
                .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler usuários do CSV", exception);
        }
    }

    private void writeAll(List<Usuario> users) {
        ensureFileExists();

        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        users.stream()
            .sorted(Comparator.comparing(Usuario::getDataCriacao))
            .map(this::toCsv)
            .forEach(lines::add);

        try {
            Files.write(arquivo, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gravar usuários no CSV", exception);
        }
    }

    private void ensureFileExists() {
        try {
            Files.createDirectories(arquivo.getParent());
            if (!Files.exists(arquivo)) {
                Files.writeString(arquivo, HEADER + System.lineSeparator(), StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível criar arquivo de usuários", exception);
        }
    }

    private String toCsv(Usuario user) {
        return String.join(",",
            escape(user.getId().toString()),
            escape(user.getNome()),
            escape(user.getEmail()),
            escape(user.getSenhaHash()),
            escape(user.getDataCriacao().toString())
        );
    }

    private Usuario fromCsv(String line) {
        List<String> columns = parseCsvLine(line);

        if (columns.size() != 5) {
            return null;
        }

        return new Usuario(
            UUID.fromString(columns.get(0)),
            columns.get(1),
            columns.get(2),
            columns.get(3),
            LocalDateTime.parse(columns.get(4))
        );
    }

    private String escape(String value) {
        String safeValue = value == null ? "" : value;

        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }

        return safeValue;
    }

    private List<String> parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (character == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        columns.add(current.toString());
        return columns;
    }
}
