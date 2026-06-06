package dev.osdiscretos.atlantidastore.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.model.Sessao;
import dev.osdiscretos.atlantidastore.model.Usuario;
import dev.osdiscretos.atlantidastore.repository.JpaJogoRepository;
import dev.osdiscretos.atlantidastore.repository.JpaSessaoRepository;
import dev.osdiscretos.atlantidastore.repository.JpaUsuarioRepository;

@Service
public class MigrationService implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(MigrationService.class);

    private final JpaUsuarioRepository usuarioRepository;
    private final JpaJogoRepository jogoRepository;
    private final JpaSessaoRepository sessaoRepository;

    public MigrationService(
        JpaUsuarioRepository usuarioRepository,
        JpaJogoRepository jogoRepository,
        JpaSessaoRepository sessaoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.jogoRepository = jogoRepository;
        this.sessaoRepository = sessaoRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Iniciando migração de dados CSV para banco de dados...");

        // Verificar se os dados já foram migrados
        if (usuarioRepository.count() > 0 || jogoRepository.count() > 0 || sessaoRepository.count() > 0) {
            logger.info("Dados já existem no banco de dados. Pulando migração.");
            return;
        }

        try {
            migrateUsuarios();
            migrateJogos();
            migrateSessoes();
            logger.info("Migração concluída com sucesso!");
        } catch (IOException e) {
            logger.error("Erro durante a migração: {}", e.getMessage(), e);
        }
    }

    private void migrateUsuarios() throws IOException {
        Path arquivo = Path.of("data", "usuarios.csv");
        if (!Files.exists(arquivo)) {
            logger.warn("Arquivo de usuários não encontrado: {}", arquivo);
            return;
        }

        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        List<Usuario> usuarios = new ArrayList<>();

        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank()) continue;

            List<String> columns = parseCsvLine(linha);
            if (columns.size() != 5) continue;

            Usuario usuario = new Usuario(
                UUID.fromString(columns.get(0)),
                columns.get(1),
                columns.get(2),
                columns.get(3),
                LocalDateTime.parse(columns.get(4))
            );
            usuarios.add(usuario);
        }

        usuarioRepository.saveAll(usuarios);
        logger.info("Migrados {} usuários", usuarios.size());
    }

    private void migrateJogos() throws IOException {
        Path arquivo = Path.of("data", "jogos.csv");
        if (!Files.exists(arquivo)) {
            logger.warn("Arquivo de jogos não encontrado: {}", arquivo);
            return;
        }

        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        List<Jogo> jogos = new ArrayList<>();

        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank()) continue;

            List<String> columns = parseCsvLine(linha);
            if (columns.size() != 8) continue;

            List<String> categorias = parseCategorias(columns.get(5));

            Jogo jogo = new Jogo(
                UUID.fromString(columns.get(0)),
                columns.get(1),
                columns.get(2),
                new BigDecimal(columns.get(3)),
                UUID.fromString(columns.get(4)),
                categorias,
                LocalDateTime.parse(columns.get(6)),
                columns.get(7)
            );
            jogos.add(jogo);
        }

        jogoRepository.saveAll(jogos);
        logger.info("Migrados {} jogos", jogos.size());
    }

    private void migrateSessoes() throws IOException {
        Path arquivo = Path.of("data", "sessoes.csv");
        if (!Files.exists(arquivo)) {
            logger.warn("Arquivo de sessões não encontrado: {}", arquivo);
            return;
        }

        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        List<Sessao> sessoes = new ArrayList<>();

        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.isBlank()) continue;

            List<String> columns = parseCsvLine(linha);
            if (columns.size() != 4) continue;

            Sessao sessao = new Sessao(
                columns.get(0),
                UUID.fromString(columns.get(1)),
                LocalDateTime.parse(columns.get(2)),
                LocalDateTime.parse(columns.get(3))
            );
            sessoes.add(sessao);
        }

        sessaoRepository.saveAll(sessoes);
        logger.info("Migradas {} sessões", sessoes.size());
    }

    private List<String> parseCategorias(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split("\\|"))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .collect(Collectors.toList());
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
