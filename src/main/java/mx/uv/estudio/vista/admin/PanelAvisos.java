package mx.uv.estudio.vista.admin;

import java.sql.SQLException;
import javax.swing.JPanel;
import mx.uv.estudio.dao.NotificacionDao;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.Avisos;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;

/** HU05 · El administrador recibe aquí el aviso cuando un fotógrafo reporta equipo dañado. */
public class PanelAvisos extends Pantalla {

    public PanelAvisos(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Avisos"));
        var avisos = new NotificacionDao().deRol("administrador");
        if (avisos.isEmpty()) {
            add(Ui.tenue("No hay avisos."));
        } else {
            JPanel p = Avisos.panel(avisos, "Enterado", this::refrescar);
            add(p);
        }
        add(Ui.tenue("El equipo reportado se atiende en la pantalla Inventario."), "gaptop 8");
    }
}
