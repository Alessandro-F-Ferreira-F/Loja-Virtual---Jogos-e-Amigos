package dev.osdiscretos.atlantidastore.dto;

public record RelacionamentoResponse(
    boolean seguindo,
    boolean solicitacaoPendente
) {
    public static RelacionamentoResponse deSeguindo() {
        return new RelacionamentoResponse(true, false);
    }

    public static RelacionamentoResponse dePendente() {
        return new RelacionamentoResponse(false, true);
    }

    public static RelacionamentoResponse deNenhum() {
        return new RelacionamentoResponse(false, false);
    }
}
