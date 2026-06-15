package dev.osdiscretos.atlantidastore.service;

public class ArquivoNaoEncontradoException extends RuntimeException {
    public ArquivoNaoEncontradoException() {
        super("Arquivo do jogo não encontrado");
    }
}
