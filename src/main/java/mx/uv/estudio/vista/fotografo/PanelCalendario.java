package mx.uv.estudio.vista.fotografo;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Window;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import mx.uv.estudio.dao.SesionDao;
import mx.uv.estudio.modelo.Equipo;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.AgendaService;
import mx.uv.estudio.servicio.Disponibilidad;
import mx.uv.estudio.servicio.InventarioService;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import net.miginfocom.swing.MigLayout;

/**
 * HU01 · Calendario del fotógrafo en vista de mes: filas de 7 días con el nombre del día arriba y el número en cada casilla.
 * Al pulsar un día se abre una ventana flotante con las citas de ese día; desde ahí se escriben las anotaciones
 * del cliente (HU04) y se asigna el equipo a la sesión (HU02).
 */
public class PanelCalendario extends Pantalla {

    private static final Locale MX = Locale.forLanguageTag("es-MX");
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMMM yyyy", MX);
    private static final DateTimeFormatter DIA_LARGO = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", MX);
    private static final int CITAS_EN_CASILLA = 2;

    private final SesionDao sesiones = new SesionDao();
    private final AgendaService agenda = new AgendaService();
    private final InventarioService inventario = new InventarioService();
    private final Disponibilidad disp = new Disponibilidad();
    private YearMonth mes = YearMonth.now();

    public PanelCalendario(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        JPanel cabecera = new JPanel(new MigLayout("insets 0, fillx", "[grow][][][]"));
        JPanel titulos = new JPanel(new MigLayout("insets 0, wrap 1"));
        titulos.add(Ui.titulo("Mi calendario"));
        titulos.add(Ui.tenue(mayuscula(MES.format(mes))));
        cabecera.add(titulos);
        cabecera.add(boton("‹ Mes anterior", () -> mes = mes.minusMonths(1)));
        cabecera.add(boton("Hoy", () -> mes = YearMonth.now()));
        cabecera.add(boton("Mes siguiente ›", () -> mes = mes.plusMonths(1)));
        add(cabecera);

        Map<LocalDate, List<Sesion>> porDia = sesiones.deFotografo(usuario.id()).stream()
                .filter(s -> !s.cancelada())
                .sorted(Comparator.comparing(Sesion::hora))
                .collect(Collectors.groupingBy(Sesion::fecha));

        JPanel cuadricula = new JPanel(new MigLayout("insets 0, gap 6, wrap 7",
                "[grow,fill,sg][grow,fill,sg][grow,fill,sg][grow,fill,sg][grow,fill,sg][grow,fill,sg][grow,fill,sg]"));
        for (DayOfWeek d : DayOfWeek.values()) {                      // lunes a domingo
            JLabel nombre = Ui.subtitulo(mayuscula(d.getDisplayName(TextStyle.FULL, MX)));
            nombre.setHorizontalAlignment(JLabel.CENTER);
            cuadricula.add(nombre);
        }
        LocalDate primero = mes.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate ultimo = mes.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        for (LocalDate dia = primero; !dia.isAfter(ultimo); dia = dia.plusDays(1)) {
            cuadricula.add(casilla(dia, porDia.getOrDefault(dia, List.of())), "h 92!");
        }
        add(cuadricula, "gaptop 8, growx");
        add(Ui.tenue("Pulsa un día para ver sus citas."), "gaptop 6");
    }

    private JButton boton(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> {
            accion.run();
            refrescar();
        });
        return b;
    }

    /** Casilla de un día: número arriba y un resumen de sus primeras citas. */
    private JButton casilla(LocalDate dia, List<Sesion> citas) {
        boolean delMes = YearMonth.from(dia).equals(mes);
        boolean hoy = dia.equals(LocalDate.now());
        String colorNumero = hoy ? "#0F6A73" : delMes ? "#111111" : "#9AA0A8";
        StringBuilder h = new StringBuilder("<html><b><font size='+1' color='" + colorNumero + "'>")
                .append(dia.getDayOfMonth()).append("</font></b>");
        for (int i = 0; i < Math.min(CITAS_EN_CASILLA, citas.size()); i++) {
            Sesion s = citas.get(i);
            h.append("<br><font size='-2' color='#0F6A73'>").append(Ui.hora(s.hora())).append("</font> <font size='-2'>")
                    .append(Ui.escapar(corto(s.clienteNombre()))).append("</font>");
        }
        if (citas.size() > CITAS_EN_CASILLA) {
            h.append("<br><font size='-2' color='#59606C'>+").append(citas.size() - CITAS_EN_CASILLA).append(" más</font>");
        }
        h.append("</html>");
        JButton b = new JButton(h.toString());
        b.setHorizontalAlignment(JButton.LEFT);
        b.setVerticalAlignment(JButton.TOP);
        if (hoy) {
            b.setBackground(new Color(0xE2F0F1));
        } else if (!citas.isEmpty() && delMes) {
            b.setBackground(new Color(0xF4FAFA));
        }
        b.addActionListener(e -> abrirDia(dia, citas));
        return b;
    }

    /** Ventana flotante con las citas del día. */
    private void abrirDia(LocalDate dia, List<Sesion> citas) {
        Window dueno = SwingUtilities.getWindowAncestor(this);
        JDialog ventana = new JDialog(dueno, "Citas del día", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel lista = new JPanel(new MigLayout("insets 14, wrap 1, fillx", "[grow,fill]"));
        lista.add(Ui.titulo(mayuscula(DIA_LARGO.format(dia))));
        lista.add(Ui.tenue(citas.isEmpty() ? "No hay citas este día."
                : citas.size() + (citas.size() == 1 ? " cita" : " citas")), "gapbottom 6");
        for (Sesion s : citas) {
            lista.add(cita(s, ventana));
        }
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        ventana.add(scroll);
        ventana.setSize(500, 540);
        ventana.setLocationRelativeTo(dueno);
        ventana.setVisible(true);
    }

    /** Una cita dentro de la ventana; el detalle (anotaciones y equipo) se abre con el botón. */
    private JPanel cita(Sesion s, JDialog ventana) {
        JPanel t = Ui.tarjeta("wrap 1, fillx", "[grow,fill]");
        t.add(Ui.subtitulo(s.clienteNombre()));
        t.add(Ui.tenue(s.horario() + " · " + s.paquete()));
        t.add(Ui.tenue(s.clienteTelefono() + " · " + s.clienteCorreo()));
        t.add(Ui.chip(s.estadoPago(), s.liquidada() ? Ui.OK : Ui.AVISO), "gapbottom 6");
        JButton alternar = new JButton("Anotaciones y equipo ▾");
        JPanel detalle = new JPanel(new MigLayout("insets 0, wrap 1, fillx", "[grow,fill]"));
        detalle.setVisible(false);
        boolean[] armado = {false};
        alternar.addActionListener(e -> {
            if (!armado[0]) {
                try {
                    armarDetalle(detalle, s, ventana);
                    armado[0] = true;
                } catch (SQLException ex) {
                    Ui.error(ventana, "No se pudo cargar el detalle: " + ex.getMessage());
                    return;
                }
            }
            detalle.setVisible(!detalle.isVisible());
            alternar.setText(detalle.isVisible() ? "Anotaciones y equipo ▴" : "Anotaciones y equipo ▾");
            ventana.revalidate();
            ventana.repaint();
        });
        t.add(alternar, "growx 0");
        t.add(detalle, "gaptop 6");
        return t;
    }

    /** HU04 · anotaciones del cliente y HU02 · equipo de la sesión. */
    private void armarDetalle(JPanel p, Sesion s, JDialog ventana) throws SQLException {
        p.add(new JLabel("Anotaciones (estilo, poses, restricciones)"));
        JTextArea notas = new JTextArea(s.anotaciones(), 4, 20);
        notas.setLineWrap(true);
        notas.setWrapStyleWord(true);
        p.add(new JScrollPane(notas));
        JButton guardarNotas = new JButton("Guardar anotaciones");
        guardarNotas.addActionListener(e -> {
            try {
                agenda.guardarAnotaciones(s.id(), notas.getText());
                Ui.info(ventana, "Anotaciones guardadas.");
            } catch (SQLException ex) {
                Ui.error(ventana, "No se pudieron guardar: " + ex.getMessage());
            }
        });
        p.add(guardarNotas, "growx 0, gapbottom 10");

        p.add(new JLabel("Equipo para esta sesión"));
        List<Integer> asignado = inventario.equipoDeSesion(s.id());
        List<JCheckBox> checks = new ArrayList<>();
        List<Equipo> visibles = new ArrayList<>();
        for (Equipo e : disp.equipoLibre(s.fecha(), s.hora(), s.duracionMin(), s.id())) {
            JCheckBox cb = new JCheckBox(e + " · " + e.categoria(), asignado.contains(e.id()));
            checks.add(cb);
            visibles.add(e);
            p.add(cb);
        }
        p.add(Ui.tenue("<html>Solo aparece el equipo disponible y sin otra sesión en este horario.</html>"));
        JButton guardarEquipo = new JButton("Guardar equipo");
        guardarEquipo.addActionListener(ev -> {
            List<Integer> ids = new ArrayList<>();
            for (int i = 0; i < checks.size(); i++) {
                if (checks.get(i).isSelected()) {
                    ids.add(visibles.get(i).id());
                }
            }
            try {
                inventario.asignar(s, ids, usuario);
                Ui.info(ventana, "Equipo asignado.");
            } catch (ReglaException ex) {
                Ui.error(ventana, Ui.lineas(ex.mensajes()));
            } catch (SQLException ex) {
                Ui.error(ventana, "No se pudo guardar: " + ex.getMessage());
            }
        });
        p.add(guardarEquipo, "growx 0");
    }

    private static String corto(String nombre) {
        return nombre.length() <= 14 ? nombre : nombre.substring(0, 13) + "…";
    }

    private static String mayuscula(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
