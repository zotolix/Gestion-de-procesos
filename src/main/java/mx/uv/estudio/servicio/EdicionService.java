package mx.uv.estudio.servicio;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mx.uv.estudio.dao.FotoDao;
import mx.uv.estudio.dao.NotificacionDao;
import mx.uv.estudio.dao.ProyectoDao;
import mx.uv.estudio.modelo.Proyecto;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.AlmacenFotos;
import mx.uv.estudio.util.Db;

/**
 * Trabajo del editor sobre un proyecto.
 * HU07 · Etiqueta el avance del proyecto completo: Pendiente → En proceso → Revisión.
 *        De Revisión lo mueve el administrador (HU20); Terminado llega con HU10.
 * HU09 · Ya aprobado, sube la versión en alta calidad (impresión) y la de baja (redes sociales).
 */
public class EdicionService {

    private static final Map<String, String> SIGUIENTE = Map.of("Pendiente", "En proceso", "En proceso", "Revisión");

    private final ProyectoDao proyectos = new ProyectoDao();
    private final FotoDao fotos = new FotoDao();
    private final NotificacionDao avisos = new NotificacionDao();

    public List<Proyecto> todos() throws SQLException {
        return proyectos.todos();
    }

    /** Estado al que el editor puede pasar el proyecto, o null si no le toca moverlo. */
    public static String siguiente(Proyecto p) {
        return SIGUIENTE.get(p.estado());
    }

    public int cuantas(Proyecto p, String calidad) throws SQLException {
        return fotos.contar(p.id(), calidad);
    }

    public void avanzar(Proyecto p, Usuario quien) throws ReglaException, SQLException {
        soloEditor(quien);
        Proyecto actual = actual(p);
        String nuevo = SIGUIENTE.get(actual.estado());
        if (nuevo == null) {
            throw new ReglaException("Un proyecto en " + actual.estado() + " no lo mueve el editor.");
        }
        proyectos.cambiarEstado(actual.id(), nuevo);
        if ("Revisión".equals(nuevo)) {
            avisos.insertar("administrador", "Proyecto listo para revisión: " + actual.cliente());
        }
    }

    public void subirCalidad(Proyecto p, String calidad, List<Path> archivos, Usuario quien)
            throws ReglaException, SQLException {
        soloEditor(quien);
        Proyecto actual = actual(p);
        if (!"Revisión".equals(actual.estado()) || !actual.aprobado()) {
            throw new ReglaException("Las dos calidades se suben cuando el administrador ya aprobó el proyecto.");
        }
        boolean alta = "Alta".equals(calidad);
        if (archivos == null || archivos.isEmpty()) {
            throw new ReglaException("Elige al menos un archivo.");
        }
        List<String> raros = Formatos.invalidos(archivos, alta ? Formatos.ALTA : Formatos.BAJA);
        if (!raros.isEmpty()) {
            throw new ReglaException((alta ? "Alta calidad: RAW, TIFF o JPG. " : "Baja calidad: solo JPG. ")
                    + "No aceptado: " + String.join(", ", raros) + ".");
        }
        List<Path> copiadas = new ArrayList<>();
        try (Connection c = Db.con()) {
            c.setAutoCommit(false);
            try {
                for (Path a : archivos) {
                    Path destino = AlmacenFotos.copiar(actual.id(), alta ? "alta" : "baja", a);
                    copiadas.add(destino);
                    fotos.insertar(c, actual.id(), destino.getFileName().toString(), calidad, destino.toString());
                }
                c.commit();
            } catch (SQLException | IOException e) {
                c.rollback();
                copiadas.forEach(AlmacenFotos::borrar);
                if (e instanceof SQLException sql) {
                    throw sql;
                }
                throw new ReglaException("No se pudo copiar un archivo: " + e.getMessage());
            }
        }
    }

    private Proyecto actual(Proyecto p) throws ReglaException, SQLException {
        return proyectos.porId(p.id()).orElseThrow(() -> new ReglaException("El proyecto ya no existe."));
    }

    private void soloEditor(Usuario quien) throws ReglaException {
        if (!quien.tiene("editor")) {
            throw new ReglaException("Solo el editor trabaja los proyectos.");
        }
    }
}
