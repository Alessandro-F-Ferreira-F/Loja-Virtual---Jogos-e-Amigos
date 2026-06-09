package dev.osdiscretos.atlantidastore.service;

import dev.osdiscretos.atlantidastore.model.Jogo;
import dev.osdiscretos.atlantidastore.repository.JogoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GameImageMigrationRunner implements ApplicationRunner {
    private final JogoRepository jogoRepository;
    private final GameImageStorageService gameImageStorageService;

    public GameImageMigrationRunner(
        JogoRepository jogoRepository,
        GameImageStorageService gameImageStorageService
    ) {
        this.jogoRepository = jogoRepository;
        this.gameImageStorageService = gameImageStorageService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Jogo jogo : jogoRepository.findAll()) {
            if (!gameImageStorageService.isDataUrl(jogo.getImagemCapa())) {
                continue;
            }

            try {
                jogo.setImagemCapa(gameImageStorageService.salvarDataUrl(jogo.getImagemCapa(), jogo.getId()));
            } catch (RuntimeException exception) {
                jogo.setImagemCapa(null);
            }

            jogoRepository.save(jogo);
        }
    }
}
