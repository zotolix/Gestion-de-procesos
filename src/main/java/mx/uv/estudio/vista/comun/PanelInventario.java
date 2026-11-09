package mx.uv.estudio.vista.comun;

import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import mx.uv.estudio.modelo.Equipo;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.InventarioService;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import net.miginfocom.swing.MigLayout;

/**
 * HU02 · Inventario de equipo y HU05 · Reportar daño.
 * Lo ven el fotógrafo (reporta daños; asigna el equipo desde su calendario) y el administrador
 * (agrega equipo y atiende el que está dañado o en mantenimiento).
 * "En uso" no es un estado guardado: se calcula solo durante el horario de la sesión.
 */
public class PanelInventario extends Pantalla {

    private final InventarioService inventario = new InventarioService();
    private final boolean admin;
    private final boolean fotografo;

    public PanelInventario(Usuario usuario, MainFrame main) {
        super(usuario, main);
        this.admin = usuario.tiene("administrador");
        this.fotografo = usuario.tiene("fotografo");
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Inventario de equipo"));
        add(Ui.tenue(admin
                ? "Agrega cámaras, lentes y accesorios, y atiende el equipo dañado o en mantenimiento. Ese equipo no se puede asignar a sesiones."
                : "Reporta el equipo dañado. Dañado y En mantenimiento los atiende el administrador y no se pueden asignar a sesiones."),
                "gapbottom 10");
        JPanel t = Ui.tarjeta("wrap 5, fillx, gap 14 8", "[grow][][][][]");
        for (String h : new String[] {"Equipo", "N.º de serie", "Categoría", "Estado", ""}) {
            t.add(Ui.tenue(h));
        }
        for (Equipo e : inventario.todo()) {
            t.add(new JLabel(e.nombre()));
            t.add(new JLabel(e.numeroSerie()));
            t.add(new JLabel(e.categoria()));
            if (e.disponible() && inventario.enUsoAhora(e)) {
                t.add(Ui.chip("En uso ahora", Ui.ACENTO));
            } else {
                t.add(Ui.chip(e.estado(), Campos.colorEquipo(e.estado())));
            }
            t.add(acciones(e));
        }
        JPanel cuerpo = new JPanel(new MigLayout("insets 0, gap 16", admin ? "[grow,fill][300!]" : "[grow,fill]"));
        cuerpo.add(t, "top");
        if (admin) {
            cuerpo.add(formulario(), "top");
        }
        add(cuerpo);
    }

    private JPanel acciones(Equipo e) {
        JPanel p = new JPanel(new MigLayout("insets 0, wrap 1, gap 2", "[grow,fill]"));
        if (e.disponible()) {
            if (fotografo) {
                JButton b = new JButton("Reportar daño");
                b.addActionListener(ev -> reportar(e));
                p.add(b);
            }
            return p;
        }
        p.add(Ui.tenue("<html>" + Ui.escapar(e.reporteTexto()) + "<br>" + Ui.escapar(e.reportePor()) + " · "
                + Ui.fecha(e.reporteFecha()) + "</html>"), "w 220!");
        if (admin) {
            JButton b = new JButton("Dañado".equals(e.estado()) ? "Pasar a mantenimiento" : "Marcar disponible");
            b.addActionListener(ev -> {
                try {
                    inventario.avanzarEstado(e, usuario);
                    refrescar();
                } catch (ReglaException ex) {
                    Ui.error(this, ex.getMessage());
                } catch (SQLException ex) {
                    Ui.error(this, "No se pudo guardar: " + ex.getMessage());
                }
            });
            p.add(b);
        }
        return p;
    }

    private JPanel formulario() {
        JPanel f = Ui.tarjeta("wrap 1, fillx", "[grow,fill]");
        f.add(Ui.subtitulo("Agregar equipo"));
        JTextField nombre = new JTextField();
        JTextField serie = new JTextField();
        JComboBox<String> categoria = new JComboBox<>(InventarioService.CATEGORIAS);
        f.add(new JLabel("Nombre"));
        f.add(nombre);
        f.add(new JLabel("Número de serie"));
        f.add(serie);
        f.add(new JLabel("Categoría"));
        f.add(categoria);
        JButton b = Ui.primario("Agregar");
        b.addActionListener(e -> {
            try {
                inventario.agregar(nombre.getText(), serie.getText(), (String) categoria.getSelectedItem(), usuario);
                refrescar();
            } catch (ReglaException ex) {
                Ui.error(this, Ui.lineas(ex.mensajes()));
            } catch (SQLException ex) {
                Ui.error(this, "No se pudo guardar: " + ex.getMessage());
            }
        });
        f.add(b, "growx 0, gaptop 6, h 36!");
        return f;
    }

    private void reportar(Equipo e) {
        JTextArea txt = new JTextArea(4, 28);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        JPanel p = new JPanel(new MigLayout("wrap 1", "[grow,fill]"));
        p.add(new JLabel("¿Qué le pasa a " + e + "?"));
        p.add(new JScrollPane(txt));
        p.add(Ui.tenue("<html>Se marca como Dañado, se quita de las sesiones que vienen y se avisa al administrador.</html>"), "w 320!");
        if (JOptionPane.showConfirmDialog(this, p, "Reportar daño", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            inventario.reportarDanio(e, txt.getText(), usuario);
            refrescar();
        } catch (ReglaException ex) {
            Ui.error(this, ex.getMessage());
        } catch (SQLException ex) {
            Ui.error(this, "No se pudo guardar: " + ex.getMessage());
        }
    }
}
