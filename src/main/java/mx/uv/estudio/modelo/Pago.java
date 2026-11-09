package mx.uv.estudio.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Pago registrado. concepto: Anticipo o Liquidación. */
public record Pago(int id, int sesionId, String concepto, BigDecimal monto, String metodo, LocalDate fecha) {

    /** Folio del recibo: 6 dígitos a partir del id. */
    public String folio() {
        return String.format("%06d", 100 + id);
    }
}
