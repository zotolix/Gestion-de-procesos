package mx.uv.estudio.modelo;

import java.math.BigDecimal;
import mx.uv.estudio.util.Ui;

/** Paquete del catálogo. duracionMin null = sin límite (bloquea el día completo). Precio con IVA incluido. */
public record Paquete(int id, String nombre, String incluye, BigDecimal precio, Integer duracionMin) {

    public String duracionLegible() {
        if (duracionMin == null) {
            return "Sin límite (bloquea el día)";
        }
        return duracionMin % 60 == 0 ? (duracionMin / 60) + (duracionMin == 60 ? " hora" : " horas") : duracionMin + " min";
    }

    @Override
    public String toString() {
        return nombre + " · " + Ui.dinero(precio);
    }
}
