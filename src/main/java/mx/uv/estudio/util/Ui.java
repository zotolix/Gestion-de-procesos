package mx.uv.estudio.util;

import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import com.formdev.flatlaf.ui.FlatLineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;

/** Estilos y utilidades compartidas por todas las pantallas. */
public final class Ui {

    public static final Color FONDO = new Color(0x1A, 0x1A, 0x1A);
    public static final Color SUPERFICIE = new Color(0x24, 0x24, 0x24);
    public static final Color BORDE = new Color(0x38, 0x38, 0x38);
    public static final Color BLANCO = new Color(0xFF, 0xFF, 0xFF);
    public static final Color OSCURO = new Color(0x1A, 0x1A, 0x1A);

    public static final Color ACENTO = new Color(0xF7, 0xFF, 0x5E);
    public static final Color OK = new Color(0x4E, 0xDF, 0x88);
    public static final Color MAL = new Color(0xFF, 0x5C, 0x5C);
    public static final Color AVISO = new Color(0xF7, 0xFF, 0x5E);
    public static final Color TENUE = new Color(0xA3, 0xA3, 0xA3);

    public static final String HEX_ACENTO = "#F7FF5E";
    public static final String HEX_OSCURO = "#1A1A1A";
    public static final String HEX_BLANCO = "#FFFFFF";
    public static final String HEX_TENUE = "#A3A3A3";
    public static final String FONT_FAMILY = FlatInterFont.FAMILY;

    private static final Locale MX = Locale.forLanguageTag("es-MX");
    private static final NumberFormat DINERO = NumberFormat.getCurrencyInstance(MX);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEE d MMM", MX);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private Ui() { }

    public static String dinero(BigDecimal v) {
        return DINERO.format(v == null ? BigDecimal.ZERO : v);
    }

    public static String fecha(LocalDate d) {
        return d == null ? "" : FECHA.format(d);
    }

    public static String hora(LocalTime t) {
        return t == null ? "" : HORA.format(t);
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(FONT_FAMILY, Font.BOLD, 28));
        l.setForeground(BLANCO);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(FONT_FAMILY, Font.BOLD, 20));
        l.setForeground(BLANCO);
        return l;
    }

    public static JLabel tenue(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(TENUE);
        return l;
    }

    /** Etiqueta de estado con punto de color, por ejemplo "● Disponible". */
    public static JLabel chip(String texto, Color color) {
        JLabel l = new JLabel("● " + texto);
        l.setForeground(color);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    public static JButton primario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(ACENTO);
        b.setForeground(OSCURO);
        b.setFont(b.getFont().deriveFont(Font.BOLD));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Panel con borde y espacio interior, para formularios y detalles. */
    public static JPanel tarjeta(String layoutConstraints) {
        return tarjeta(layoutConstraints, "");
    }

    public static JPanel tarjeta(String layoutConstraints, String columnas) {
        JPanel p = new JPanel(new MigLayout(layoutConstraints, columnas));
        p.setBackground(SUPERFICIE);
        Color borde = UIManager.getColor("Component.borderColor");
        Color cBorde = (borde != null) ? borde : BORDE;
        p.setBorder(BorderFactory.createCompoundBorder(
                new FlatLineBorder(new Insets(1, 1, 1, 1), cBorde, 1f, 16),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return p;
    }

    /** Une varios mensajes en un texto HTML de varias líneas. */
    public static String lineas(List<String> mensajes) {
        StringBuilder sb = new StringBuilder("<html>");
        for (String m : mensajes) {
            sb.append(escapar(m)).append("<br>");
        }
        return sb.append("</html>").toString();
    }

    public static String escapar(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
    }

    /** Convierte texto como "1,500.50" o "$750" a número. Devuelve null si no es número. */
    public static BigDecimal leerMonto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.replace("$", "").replace(",", "").trim();
        if (limpio.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(limpio);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
