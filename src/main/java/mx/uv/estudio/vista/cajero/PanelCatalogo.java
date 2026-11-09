package mx.uv.estudio.vista.cajero;

import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mx.uv.estudio.modelo.Paquete;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.PaqueteService;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import net.miginfocom.swing.MigLayout;

/** HU11 · Catálogo de paquetes. Los álbumes vienen incluidos en los paquetes. */
public class PanelCatalogo extends Pantalla {

    public PanelCatalogo(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Catálogo de paquetes"));
        add(Ui.tenue("Precios finales con IVA incluido. Al agendar se paga mínimo el 50 %; también se puede pagar el total."), "gapbottom 10");
        JPanel fila = new JPanel(new MigLayout("insets 0, gap 14, wrap 3", "[grow,fill,sg][grow,fill,sg][grow,fill,sg]"));
        for (Paquete p : new PaqueteService().todos()) {
            JPanel t = Ui.tarjeta("wrap 1, fillx", "[grow,fill]");
            t.add(Ui.subtitulo(p.nombre()));
            JLabel precio = Ui.titulo(Ui.dinero(p.precio()));
            precio.setForeground(Ui.ACENTO);
            t.add(precio);
            t.add(Ui.tenue("IVA incluido"));
            t.add(new JLabel("<html>" + Ui.escapar(p.incluye()).replace(" · ", "<br>") + "</html>"), "gaptop 8");
            t.add(Ui.tenue("Duración: " + p.duracionLegible()), "gaptop 8");
            fila.add(t, "top");
        }
        add(fila);
    }
}
