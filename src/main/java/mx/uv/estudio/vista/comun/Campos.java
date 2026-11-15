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
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
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

    @FunctionalInterface
    public interface ValidadorTexto {
        boolean esValido(String textoActual, String textoPropuesto);
    }

    /** Aplica un DocumentFilter a un JTextField para restringir en tiempo real los caracteres aceptados. */
    public static void aplicarFiltro(JTextField tf, ValidadorTexto validador) {
        if (tf == null || !(tf.getDocument() instanceof AbstractDocument doc)) {
            return;
        }
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) {
                    return;
                }
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String propuesto = new StringBuilder(actual).insert(offset, string).toString();
                if (validador.esValido(actual, propuesto)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String reemplazo = (text == null) ? "" : text;
                String propuesto = new StringBuilder(actual).replace(offset, offset + length, reemplazo).toString();
                if (validador.esValido(actual, propuesto)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    /** Permite únicamente letras (incluyendo acentos, ñ y diéresis), espacios, apóstrofes y guiones. */
    public static void soloLetras(JTextField tf) {
        aplicarFiltro(tf, (actual, nuevo) -> nuevo.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'\\-]*$"));
    }

    /** Permite únicamente caracteres telefónicos (dígitos, espacios, +, -, paréntesis). */
    public static void soloTelefono(JTextField tf) {
        aplicarFiltro(tf, (actual, nuevo) -> nuevo.matches("^[0-9 ()+\\-]*$"));
    }

    /** Permite únicamente cantidades monetarias y números decimales positivos (hasta 2 decimales). */
    public static void soloDecimal(JTextField tf) {
        aplicarFiltro(tf, (actual, nuevo) -> nuevo.matches("^\\d*(\\.\\d{0,2})?$"));
    }

    /** Permite únicamente caracteres para número de serie (alfanumérico y guiones). */
    public static void soloSerie(JTextField tf) {
        aplicarFiltro(tf, (actual, nuevo) -> nuevo.matches("^[a-zA-Z0-9\\-]*$"));
    }
}
