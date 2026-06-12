package dev.osdiscretos.atlantidastore.dto;

public record RelacionamentoResponse(
    boolean seguindo,
    boolean solicitacaoPendente
) {
    public static RelacionamentoResponse seguindo() {
        return new RelacionamentoResponse(true, false);
    }

    public static RelacionamentoResponse pendente() {
        return new RelacionamentoResponse(false, true);
    }

    public static RelacionamentoResponse nenhum() {
        return new RelacionamentoResponse(false, false);
    }
}
