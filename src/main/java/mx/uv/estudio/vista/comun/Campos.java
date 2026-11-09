package mx.uv.estudio.vista.comun;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import mx.uv.estudio.util.Ui;

/** Campos de fecha y hora y colores de estado que usan varias pantallas. */
public final class Campos {

    private Campos() { }

    public static JSpinner fecha(LocalDate valor) {
        JSpinner sp = new JSpinner(new SpinnerDateModel(aDate(valor.atStartOfDay()), null, null, Calendar.DAY_OF_MONTH));
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        return sp;
    }

    /** Hora libre en formato 24 h (no hay horario del estudio confirmado). */
    public static JSpinner hora(LocalTime valor) {
        JSpinner sp = new JSpinner(new SpinnerDateModel(aDate(LocalDate.now().atTime(valor)), null, null, Calendar.MINUTE));
        sp.setEditor(new JSpinner.DateEditor(sp, "HH:mm"));
        return sp;
    }

    public static LocalDate leerFecha(JSpinner sp) {
        return ((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalTime leerHora(JSpinner sp) {
        return ((Date) sp.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime().truncatedTo(ChronoUnit.MINUTES);
    }

    /** Siguiente hora en punto, para proponer al agendar. */
    public static LocalTime siguienteHora() {
        return LocalTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1);
    }

    private static Date aDate(LocalDateTime t) {
        return Date.from(t.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Color colorEquipo(String estado) {
        return switch (estado) {
            case "Disponible" -> Ui.OK;
            case "Dañado" -> Ui.MAL;
            default -> Ui.AVISO;
        };
    }
}
