package mx.uv.estudio.vista.editor;

import java.sql.SQLException;
import mx.uv.estudio.dao.NotificacionDao;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.Avisos;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;

/** HU20 · El editor ve aquí si el administrador aprobó un proyecto o lo regresó con un comentario. */
public class PanelAvisosEditor extends Pantalla {

    public PanelAvisosEditor(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Avisos del editor"));
        var avisos = new NotificacionDao().deRol("editor");
        if (avisos.isEmpty()) {
            add(Ui.tenue("No hay avisos."));
        } else {
            add(Avisos.panel(avisos, "Enterado", this::refrescar));
        }
        add(Ui.tenue("Las demás pantallas del editor llegan en el Sprint 3."), "gaptop 8");
    }
}
