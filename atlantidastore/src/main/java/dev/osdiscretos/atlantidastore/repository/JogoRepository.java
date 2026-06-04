package dev.osdiscretos.atlantidastore.repository;

import dev.osdiscretos.atlantidastore.model.Jogo;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.math.BigDecimal;
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
public class JogoRepository {
    private static final String HEADER = "id,titulo,descricao,preco,publicadorId,categorias,dataCriacao,downloadUrl";

    private final Path arquivo = Path.of("data", "jogos.csv");

    public Jogo save(Jogo jogo) {
        synchronized (this) {
            List<Jogo> jogos = new ArrayList<>(findAll());
            jogos.removeIf(existingGame -> existingGame.getId().equals(jogo.getId()));
            jogos.add(jogo);
            writeAll(jogos);
            return jogo;
        }
    }

    public Jogo findById(UUID id) {
        return findAll().stream()
            .filter(jogo -> jogo.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public List<Jogo> findAll() {
        return readAll().stream()
            .sorted(Comparator.comparing(Jogo::getDataCriacao))
            .toList();
    }

    public boolean existsByTituloIgnoreCase(String titulo) {
        if (titulo == null) {
            return false;
        }

        String normalizedTitle = titulo.trim();

        return findAll().stream()
            .anyMatch(jogo -> jogo.getTitulo().equalsIgnoreCase(normalizedTitle));
    }

    public void deleteById(UUID id) {
        synchronized (this) {
            List<Jogo> jogos = new ArrayList<>(findAll());
            jogos.removeIf(jogo -> jogo.getId().equals(id));
            writeAll(jogos);
        }
    }

    private List<Jogo> readAll() {
        ensureFileExists();

        try {
            return Files.readAllLines(arquivo, StandardCharsets.UTF_8).stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(this::fromCsv)
                .filter(Objects::nonNull)
                .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler jogos do CSV", exception);
        }
    }

    private void writeAll(List<Jogo> jogos) {
        ensureFileExists();

        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        jogos.stream()
            .sorted(Comparator.comparing(Jogo::getDataCriacao))
            .map(this::toCsv)
            .forEach(lines::add);

        try {
            Files.write(arquivo, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gravar jogos no CSV", exception);
        }
    }

    private void ensureFileExists() {
        try {
            Files.createDirectories(arquivo.getParent());
            if (!Files.exists(arquivo)) {
                Files.writeString(arquivo, HEADER + System.lineSeparator(), StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível criar arquivo de jogos", exception);
        }
    }

    private String toCsv(Jogo jogo) {
        return String.join(",",
            escape(jogo.getId().toString()),
            escape(jogo.getTitulo()),
            escape(jogo.getDescricao()),
            escape(jogo.getPreco().toPlainString()),
            escape(jogo.getPublicadorId().toString()),
            escape(String.join("|", jogo.getCategorias())),
            escape(jogo.getDataCriacao().toString()),
            escape(jogo.getDownloadUrl())
        );
    }

    private Jogo fromCsv(String line) {
        List<String> columns = parseCsvLine(line);

        if (columns.size() != 8) {
            return null;
        }

        return new Jogo(
            UUID.fromString(columns.get(0)),
            columns.get(1),
            columns.get(2),
            new BigDecimal(columns.get(3)),
            UUID.fromString(columns.get(4)),
            parseCategorias(columns.get(5)),
            LocalDateTime.parse(columns.get(6)),
            columns.get(7)
        );
    }

    private List<String> parseCategorias(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return List.of(value.split("\\|")).stream()
            .map(String::trim)
            .filter(categoria -> !categoria.isBlank())
            .toList();
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
