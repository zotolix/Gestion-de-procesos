package mx.uv.estudio.servicio;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.uv.estudio.dao.FotoDao;
import mx.uv.estudio.dao.NotificacionDao;
import mx.uv.estudio.dao.ProyectoDao;
import mx.uv.estudio.dao.SesionDao;
import mx.uv.estudio.modelo.Proyecto;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.AlmacenFotos;
import mx.uv.estudio.util.Db;

/**
 * HU03 · El fotógrafo sube las fotos de una sesión realizada y el proyecto queda en Pendiente.
 * HU06 · Al subirlas, el editor recibe un aviso con el cliente y el número de fotos.
 */
public class FotoService {

    private final SesionDao sesiones = new SesionDao();
    private final ProyectoDao proyectos = new ProyectoDao();
    private final FotoDao fotos = new FotoDao();
    private final NotificacionDao avisos = new NotificacionDao();

    /** Sesiones del fotógrafo ya realizadas que aún no tienen proyecto o cuyo proyecto sigue en Pendiente. */
    public List<Sesion> sesionesPorSubir(Usuario fotografo) throws SQLException {
        List<Sesion> r = new ArrayList<>();
        for (Sesion s : sesiones.deFotografo(fotografo.id())) {
            if (s.cancelada() || s.inicio().isAfter(LocalDateTime.now())) {
                continue;
            }
            Optional<Proyecto> p = proyectos.porSesion(s.id());
            if (p.isEmpty() || "Pendiente".equals(p.get().estado())) {
                r.add(s);
            }
        }
        return r;
    }

    public Optional<Proyecto> proyectoDe(Sesion s) throws SQLException {
        return proyectos.porSesion(s.id());
    }

    public List<Proyecto> proyectosDe(Usuario fotografo) throws SQLException {
        return proyectos.deFotografo(fotografo.id());
    }

    public void subirFotos(Sesion s, List<Path> archivos, Usuario quien) throws ReglaException, SQLException {
        if (!quien.tiene("fotografo") || s.fotografoId() != quien.id()) {
            throw new ReglaException("Solo el fotógrafo de la sesión puede subir sus fotos.");
        }
        if (s.cancelada()) {
            throw new ReglaException("La sesión está cancelada.");
        }
        if (s.inicio().isAfter(LocalDateTime.now())) {
            throw new ReglaException("La sesión todavía no se realiza.");
        }
        if (archivos == null || archivos.isEmpty()) {
            throw new ReglaException("Elige al menos una foto.");
        }
        List<String> raros = Formatos.invalidos(archivos, Formatos.ORIGINALES);
        if (!raros.isEmpty()) {
            throw new ReglaException("Formato no aceptado: " + String.join(", ", raros) + ".");
        }
        Optional<Proyecto> existente = proyectos.porSesion(s.id());
        if (existente.isPresent() && !"Pendiente".equals(existente.get().estado())) {
            throw new ReglaException("El proyecto ya está en " + existente.get().estado()
                    + "; solo se pueden agregar fotos mientras está Pendiente.");
        }

        List<Path> copiadas = new ArrayList<>();
        try (Connection c = Db.con()) {
            c.setAutoCommit(false);
            try {
                int proyectoId = existente.isPresent()
                        ? existente.get().id()
                        : proyectos.crear(c, s.id(), 0);
                for (Path a : archivos) {
                    Path destino = AlmacenFotos.copiar(proyectoId, "original", a);
                    copiadas.add(destino);
                    fotos.insertar(c, proyectoId, destino.getFileName().toString(), "Original", destino.toString());
                }
                proyectos.sumarFotos(c, proyectoId, archivos.size());
                c.commit();
            } catch (SQLException | IOException e) {
                c.rollback();
                copiadas.forEach(AlmacenFotos::borrar);
                if (e instanceof SQLException sql) {
                    throw sql;
                }
                throw new ReglaException("No se pudo copiar una de las fotos: " + e.getMessage());
            }
        }
        // HU06: aviso al editor
        avisos.insertar("editor", "Fotos nuevas: " + s.clienteNombre() + " · " + archivos.size()
                + (archivos.size() == 1 ? " foto" : " fotos"));
    }
}
