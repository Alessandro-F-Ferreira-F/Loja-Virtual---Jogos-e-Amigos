package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Sessao;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class SessaoRepository {
    private static final String HEADER = "token,usuarioId,criadoEm,expiraEm";

    private final Path arquivo = Path.of("data", "sessoes.csv");

    public Sessao save(Sessao sessao) {
        synchronized (this) {
            List<Sessao> sessoes = new ArrayList<>(readAll());
            sessoes.removeIf(existingSession -> existingSession.getToken().equals(sessao.getToken()));
            sessoes.add(sessao);
            writeAll(sessoes);
            return sessao;
        }
    }

    public Sessao findByToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        Sessao sessao = readAll().stream()
            .filter(currentSession -> currentSession.getToken().equals(token))
            .findFirst()
            .orElse(null);

        if (sessao != null && sessao.isExpirada()) {
            removeByToken(token);
            return null;
        }

        return sessao;
    }

    public void removeByToken(String token) {
        synchronized (this) {
            List<Sessao> sessoes = new ArrayList<>(readAll());
            sessoes.removeIf(sessao -> sessao.getToken().equals(token));
            writeAll(sessoes);
        }
    }

    public void removeByUsuarioId(UUID usuarioId) {
        synchronized (this) {
            List<Sessao> sessoes = new ArrayList<>(readAll());
            sessoes.removeIf(sessao -> sessao.getUsuarioId().equals(usuarioId));
            writeAll(sessoes);
        }
    }

    public void removeExpired() {
        synchronized (this) {
            List<Sessao> sessoes = new ArrayList<>(readAll());
            sessoes.removeIf(Sessao::isExpirada);
            writeAll(sessoes);
        }
    }

    private List<Sessao> readAll() {
        ensureFileExists();

        try {
            return Files.readAllLines(arquivo, StandardCharsets.UTF_8).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(this::fromCsv)
                .filter(Objects::nonNull)
                .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler sessões do CSV", exception);
        }
    }

    private void writeAll(List<Sessao> sessoes) {
        ensureFileExists();

        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        sessoes.stream()
            .map(this::toCsv)
            .forEach(lines::add);

        try {
            Files.write(arquivo, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gravar sessões no CSV", exception);
        }
    }

    private void ensureFileExists() {
        try {
            Files.createDirectories(arquivo.getParent());
            if (!Files.exists(arquivo)) {
                Files.writeString(arquivo, HEADER + System.lineSeparator(), StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível criar arquivo de sessões", exception);
        }
    }

    private String toCsv(Sessao sessao) {
        return String.join(",",
            escape(sessao.getToken()),
            escape(sessao.getUsuarioId().toString()),
            escape(sessao.getCriadoEm().toString()),
            escape(sessao.getExpiraEm().toString())
        );
    }

    private Sessao fromCsv(String line) {
        List<String> columns = parseCsvLine(line);

        if (columns.size() != 4) {
            return null;
        }

        return new Sessao(
            columns.get(0),
            UUID.fromString(columns.get(1)),
            LocalDateTime.parse(columns.get(2)),
            LocalDateTime.parse(columns.get(3))
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
