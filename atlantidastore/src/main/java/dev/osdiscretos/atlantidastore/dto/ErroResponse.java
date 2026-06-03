package dev.osdiscretos.atlantidastore.dto;

public record ErroResponse(String mensagem) {
    public ErroResponse {
        System.out.println("Erro: " + mensagem);
    }
}
