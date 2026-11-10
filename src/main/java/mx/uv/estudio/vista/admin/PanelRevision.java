package mx.uv.estudio.vista.admin;

import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import mx.uv.estudio.modelo.Proyecto;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.RevisionService;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import net.miginfocom.swing.MigLayout;

/** HU20 · El administrador revisa los proyectos en Revisión: los aprueba o los regresa a En proceso con comentario. */
public class PanelRevision extends Pantalla {

    private final RevisionService servicio = new RevisionService();

    public PanelRevision(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Revisión de proyectos"));
        add(Ui.tenue("<html>Aprueba el proyecto para que el editor suba las dos calidades y lo marque Terminado, "
                + "o regrésalo a En proceso con un comentario.</html>"), "gapbottom 10");
        var pendientes = servicio.porRevisar();
        if (pendientes.isEmpty()) {
            add(Ui.tenue("No hay proyectos en Revisión."));
        }
        for (Proyecto p : pendientes) {
            add(tarjeta(p));
        }
        var aprobados = servicio.aprobadosEnEspera();
        if (!aprobados.isEmpty()) {
            add(Ui.subtitulo("Aprobados, esperando al editor"), "gaptop 14");
            for (Proyecto p : aprobados) {
                add(Ui.chip(p.cliente() + " · " + p.numFotos() + " fotos", Ui.OK));
            }
        }
    }

    private JPanel tarjeta(Proyecto p) {
        JPanel t = Ui.tarjeta("fillx, gap 8", "[grow,fill][][]");
        t.add(new JLabel("<html><b>" + Ui.escapar(p.cliente()) + "</b><br><font color='" + Ui.HEX_TENUE + "'>Sesión del "
                + Ui.fecha(p.fechaSesion()) + " · " + p.numFotos() + " fotos</font></html>"));
        t.add(Accion.boton(this, "Aprobar", true, () -> servicio.aprobar(p, usuario), () -> {
            Ui.info(this, "Proyecto aprobado. Se avisó al editor.");
            refrescar();
        }));
        JButton regresar = new JButton("Regresar a En proceso");
        regresar.addActionListener(e -> regresar(p));
        t.add(regresar);
        return t;
    }

    private void regresar(Proyecto p) {
        JTextArea txt = new JTextArea(4, 28);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        JPanel c = new JPanel(new MigLayout("wrap 1", "[grow,fill]"));
        c.add(new JLabel("¿Qué debe corregir el editor en el proyecto de " + p.cliente() + "?"));
        c.add(new JScrollPane(txt));
        if (JOptionPane.showConfirmDialog(this, c, "Regresar a En proceso", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            servicio.regresar(p, txt.getText(), usuario);
            refrescar();
        } catch (ReglaException ex) {
            Ui.error(this, Ui.lineas(ex.mensajes()));
        } catch (SQLException ex) {
            Ui.error(this, "No se pudo guardar: " + ex.getMessage());
        }
    }
}
