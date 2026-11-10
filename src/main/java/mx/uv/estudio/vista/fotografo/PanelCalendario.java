package mx.uv.estudio.vista.fotografo;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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

/** HU01 · Calendario del fotógrafo, HU04 · Anotaciones del cliente y asignación de equipo a su sesión (HU02). */
public class PanelCalendario extends Pantalla {

    private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("EEE d", Locale.forLanguageTag("es-MX"));
    private final SesionDao sesiones = new SesionDao();
    private final AgendaService agenda = new AgendaService();
    private final InventarioService inventario = new InventarioService();
    private final Disponibilidad disp = new Disponibilidad();
    private int semana;              // 0 = semana actual
    private Integer seleccionada;    // id de la sesión abierta en el detalle

    public PanelCalendario(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        LocalDate lunes = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(semana);
        JPanel cabecera = new JPanel(new MigLayout("insets 0, fillx", "[grow][][][]"));
        JPanel titulos = new JPanel(new MigLayout("insets 0, wrap 1"));
        titulos.add(Ui.titulo("Mi calendario"));
        titulos.add(Ui.tenue("Semana del " + Ui.fecha(lunes) + " al " + Ui.fecha(lunes.plusDays(6))));
        cabecera.add(titulos);
        cabecera.add(boton("‹ Anterior", () -> semana--));
        cabecera.add(boton("Hoy", () -> semana = 0));
        cabecera.add(boton("Siguiente ›", () -> semana++));
        add(cabecera);

        List<Sesion> mias = sesiones.deFotografo(usuario.id());
        JPanel semanaPanel = new JPanel(new MigLayout("insets 0, gap 8, wrap 7",
                "[grow,fill,sg][grow,fill,sg][grow,fill,sg][grow,fill,sg][grow,fill,sg][grow,fill,sg][grow,fill,sg]"));
        Sesion detalle = null;
        for (int i = 0; i < 7; i++) {
            LocalDate dia = lunes.plusDays(i);
            JPanel col = Ui.tarjeta("insets 8, wrap 1, fillx", "[grow,fill]");
            JLabel h = Ui.subtitulo(DIA.format(dia));
            if (dia.equals(LocalDate.now())) {
                h.setForeground(Ui.ACENTO);
            }
            col.add(h);
            boolean libre = true;
            for (Sesion s : mias) {
                if (!s.fecha().equals(dia)) {
                    continue;
                }
                libre = false;
                JButton b = new JButton("<html><b>" + s.horario() + "</b><br>"
                        + Ui.escapar(s.clienteNombre()) + "<br><font color='" + Ui.HEX_TENUE + "'>" + Ui.escapar(s.paquete()) + "</font></html>");
                b.setHorizontalAlignment(JButton.LEFT);
                if (seleccionada != null && seleccionada == s.id()) {
                    b.setBackground(Ui.ACENTO);
                    b.setForeground(Ui.OSCURO);
                    detalle = s;
                }
                b.addActionListener(e -> {
                    seleccionada = s.id();
                    refrescar();
                });
                col.add(b);
            }
            if (libre) {
                col.add(Ui.tenue("Libre"));
            }
            semanaPanel.add(col, "top, h 160:pref");
        }

        JPanel cuerpo = new JPanel(new MigLayout("insets 0, gap 16", "[grow,fill][340!]"));
        cuerpo.add(semanaPanel, "top");
        cuerpo.add(detalle == null ? vacio() : detalle(detalle), "top");
        add(cuerpo, "gaptop 8");
    }

    private JButton boton(String texto, Runnable accion) {
        JButton b = new JButton(texto);
        b.addActionListener(e -> {
            accion.run();
            refrescar();
        });
        return b;
    }

    private JPanel vacio() {
        JPanel p = Ui.tarjeta("wrap 1, fillx", "[grow,fill]");
        p.add(Ui.subtitulo("Detalle de la sesión"));
        p.add(Ui.tenue("<html>Elige una sesión del calendario para ver al cliente, escribir sus anotaciones y asignar equipo. Las sesiones las agenda el cajero.</html>"));
        return p;
    }

    private JPanel detalle(Sesion s) throws SQLException {
        JPanel p = Ui.tarjeta("wrap 1, fillx", "[grow,fill]");
        p.add(Ui.subtitulo(s.clienteNombre()));
        p.add(Ui.tenue(Ui.fecha(s.fecha()) + " · " + s.horario() + " · " + s.paquete()));
        p.add(Ui.tenue(s.clienteTelefono() + " · " + s.clienteCorreo()));
        p.add(Ui.chip(s.estadoPago(), s.liquidada() ? Ui.OK : Ui.AVISO), "gapbottom 8");

        p.add(new JLabel("Anotaciones (estilo, poses, restricciones)"));
        JTextArea notas = new JTextArea(s.anotaciones(), 4, 20);
        notas.setLineWrap(true);
        notas.setWrapStyleWord(true);
        p.add(new JScrollPane(notas));
        JButton guardarNotas = new JButton("Guardar anotaciones");
        guardarNotas.addActionListener(e -> {
            try {
                agenda.guardarAnotaciones(s.id(), notas.getText());
                Ui.info(this, "Anotaciones guardadas.");
            } catch (SQLException ex) {
                Ui.error(this, "No se pudieron guardar: " + ex.getMessage());
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
                Ui.info(this, "Equipo asignado.");
            } catch (ReglaException ex) {
                Ui.error(this, Ui.lineas(ex.mensajes()));
            } catch (SQLException ex) {
                Ui.error(this, "No se pudo guardar: " + ex.getMessage());
            }
        });
        p.add(guardarEquipo, "growx 0");
        return p;
    }
}
