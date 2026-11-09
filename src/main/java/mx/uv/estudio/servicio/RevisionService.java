package mx.uv.estudio.servicio;

import java.sql.SQLException;
import java.util.List;
import mx.uv.estudio.dao.NotificacionDao;
import mx.uv.estudio.dao.ProyectoDao;
import mx.uv.estudio.modelo.Proyecto;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Usuario;

/**
 * HU20 · El administrador revisa los proyectos que están en Revisión antes de que se terminen:
 * los aprueba, o los regresa a En proceso con un comentario que ve el editor.
 */
public class RevisionService {

    private final ProyectoDao proyectos = new ProyectoDao();
    private final NotificacionDao avisos = new NotificacionDao();

    public List<Proyecto> porRevisar() throws SQLException {
        return proyectos.porRevisar();
    }

    public List<Proyecto> aprobadosEnEspera() throws SQLException {
        return proyectos.aprobadosEnEspera();
    }

    public void aprobar(Proyecto p, Usuario quien) throws ReglaException, SQLException {
        validar(p, quien);
        proyectos.aprobar(p.id());
        avisos.insertar("editor", "Aprobado por el administrador: " + p.cliente()
                + ". Ya puedes subir las dos calidades y marcarlo Terminado.");
    }

    public void regresar(Proyecto p, String comentario, Usuario quien) throws ReglaException, SQLException {
        validar(p, quien);
        if (comentario == null || comentario.isBlank()) {
            throw new ReglaException("Escribe un comentario para que el editor sepa qué corregir.");
        }
        proyectos.regresar(p.id(), comentario.trim());
        avisos.insertar("editor", "Regresado a En proceso: " + p.cliente() + " · " + comentario.trim());
    }

    private void validar(Proyecto p, Usuario quien) throws ReglaException {
        if (!quien.tiene("administrador")) {
            throw new ReglaException("Solo el administrador revisa los proyectos.");
        }
        if (!"Revisión".equals(p.estado()) || p.aprobado()) {
            throw new ReglaException("Ese proyecto ya no está pendiente de revisión.");
        }
    }
}
