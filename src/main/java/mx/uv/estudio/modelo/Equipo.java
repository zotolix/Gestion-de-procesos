package mx.uv.estudio.modelo;

import java.time.LocalDate;

/** Equipo del estudio. categoria: Cámara, Lente o Accesorio. estado: Disponible, Dañado o En mantenimiento. */
public record Equipo(int id, String nombre, String numeroSerie, String categoria, String estado,
                     String reporteTexto, String reportePor, LocalDate reporteFecha) {

    public boolean disponible() {
        return "Disponible".equals(estado);
    }

    @Override
    public String toString() {
        return nombre + " (" + numeroSerie + ")";
    }
}
