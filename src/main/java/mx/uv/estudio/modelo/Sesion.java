package mx.uv.estudio.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import mx.uv.estudio.util.Ui;

/** Sesión agendada, con los datos del cliente y lo que lleva pagado. */
public record Sesion(int id, LocalDate fecha, LocalTime hora,
                     int fotografoId, String fotografo,
                     int paqueteId, String paquete, BigDecimal precio, Integer duracionMin,
                     String clienteNombre, String clienteTelefono, String clienteCorreo,
                     String anotaciones, String estado, BigDecimal pagado) {

    public boolean cancelada() {
        return "Cancelada".equals(estado);
    }

    public BigDecimal saldo() {
        return precio.subtract(pagado);
    }

    public boolean liquidada() {
        return saldo().signum() <= 0;
    }

    /** Estado de pago (pregunta 3). */
    public String estadoPago() {
        return liquidada() ? "Liquidado" : "Anticipo pagado";
    }

    public LocalDateTime inicio() {
        return duracionMin == null ? fecha.atStartOfDay() : fecha.atTime(hora);
    }

    public LocalDateTime fin() {
        return duracionMin == null ? fecha.plusDays(1).atStartOfDay() : fecha.atTime(hora).plusMinutes(duracionMin);
    }

    public String horario() {
        return duracionMin == null ? Ui.hora(hora) + " · todo el día" : Ui.hora(hora) + "–" + Ui.hora(hora.plusMinutes(duracionMin));
    }
}
