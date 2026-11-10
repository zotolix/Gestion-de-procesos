package mx.uv.estudio.vista;

import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mx.uv.estudio.dao.NotificacionDao;
import mx.uv.estudio.modelo.Notificacion;
import mx.uv.estudio.util.Ui;
import net.miginfocom.swing.MigLayout;

/** Lista de avisos dentro del sistema (HU06 para el editor, HU10 para el cajero). */
public final class Avisos {

    private Avisos() { }

    public static JPanel panel(List<Notificacion> lista, String textoBoton, Runnable alCambiar) {
        JPanel p = Ui.tarjeta("insets 10 14, wrap 2, fillx, gap 6", "[grow,fill][]");
        p.add(Ui.subtitulo("Avisos"), "span 2");
        NotificacionDao dao = new NotificacionDao();
        for (Notificacion n : lista) {
            JLabel l = new JLabel("<html>" + Ui.escapar(n.texto()) + " <font color='" + Ui.HEX_TENUE + "'>· " + Ui.fecha(n.fecha()) + "</font></html>");
            if (n.leida()) {
                l.setForeground(Ui.TENUE);
            }
            p.add(l);
            if (n.leida()) {
                p.add(Ui.tenue("Leído"));
            } else {
                JButton b = new JButton(textoBoton);
                b.addActionListener(e -> {
                    try {
                        dao.marcarLeida(n.id());
                    } catch (SQLException ex) {
                        Ui.error(p, "No se pudo marcar el aviso.");
                    }
                    alCambiar.run();
                });
                p.add(b);
            }
        }
        return p;
    }
}
