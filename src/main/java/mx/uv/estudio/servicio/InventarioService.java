package mx.uv.estudio.servicio;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import mx.uv.estudio.dao.EquipoDao;
import mx.uv.estudio.dao.NotificacionDao;
import mx.uv.estudio.dao.SesionDao;
import mx.uv.estudio.modelo.Equipo;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Usuario;

/** HU02 · Inventario (el administrador agrega equipo, el fotógrafo lo asigna) y HU05 · Reportar daño. */
public class InventarioService {

    public static final String[] CATEGORIAS = {"Cámara", "Lente", "Accesorio"};

    private final EquipoDao equipo = new EquipoDao();
    private final SesionDao sesiones = new SesionDao();
    private final NotificacionDao avisos = new NotificacionDao();
    private final Disponibilidad disp = new Disponibilidad();

    public List<Equipo> todo() throws SQLException {
        return equipo.todos();
    }

    public boolean enUsoAhora(Equipo e) throws SQLException {
        return disp.enUsoAhora(e);
    }

    /** Lo hace el administrador: cámaras, lentes y accesorios nuevos. */
    public void agregar(String nombre, String serie, String categoria, Usuario quien) throws ReglaException, SQLException {
        if (!quien.tiene("administrador")) {
            throw new ReglaException("Solo el administrador agrega equipo al inventario.");
        }
        List<String> err = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) {
            err.add("Escribe el nombre del equipo.");
        }
        if (serie == null || serie.isBlank()) {
            err.add("Escribe el número de serie.");
        }
        if (serie != null && !serie.isBlank()) {
            for (Equipo e : equipo.todos()) {
                if (e.numeroSerie().equalsIgnoreCase(serie.trim())) {
                    err.add("Ya existe un equipo con el número de serie " + serie.trim() + " (" + e.nombre() + ").");
                    break;
                }
            }
        }
        if (!err.isEmpty()) {
            throw new ReglaException(err);
        }
        equipo.insertar(nombre.trim(), serie.trim(), categoria);
    }

    public void reportarDanio(Equipo e, String descripcion, Usuario fotografo) throws ReglaException, SQLException {
        if (!fotografo.tiene("fotografo")) {
            throw new ReglaException("Solo el fotógrafo reporta equipo dañado.");
        }
        if (!e.disponible()) {
            throw new ReglaException("Ese equipo ya está " + e.estado().toLowerCase() + ".");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaException("Describe el daño antes de enviar el reporte.");
        }
        equipo.reportarDanio(e.id(), descripcion.trim(), fotografo.nombre(), LocalDate.now());
        avisos.insertar("administrador", "Equipo dañado: " + e + " · " + descripcion.trim() + " · " + fotografo.nombre());
    }

    public List<Integer> equipoDeSesion(int sesionId) throws SQLException {
        return sesiones.equipoDe(sesionId);
    }

    /** El fotógrafo asigna equipo a su sesión: debe estar disponible y libre en ese horario. */
    public void asignar(Sesion s, List<Integer> equipoIds, Usuario fotografo) throws ReglaException, SQLException {
        if (!fotografo.tiene("fotografo") || s.fotografoId() != fotografo.id()) {
            throw new ReglaException("Cada fotógrafo asigna equipo solo a sus propias sesiones.");
        }
        List<String> err = new ArrayList<>();
        List<Integer> antes = sesiones.equipoDe(s.id());
        for (int id : equipoIds) {
            Equipo e = equipo.porId(id).orElseThrow();
            if (!e.disponible() && !antes.contains(id)) {
                err.add(e + " está " + e.estado().toLowerCase() + " y no se puede asignar.");
            }
            disp.choqueEquipo(id, s.fecha(), s.hora(), s.duracionMin(), s.id()).ifPresent(o ->
                    err.add(e + " ya está en la sesión de " + o.clienteNombre() + " (" + o.horario() + ")."));
        }
        if (!err.isEmpty()) {
            throw new ReglaException(err);
        }
        sesiones.asignarEquipo(s.id(), equipoIds);
    }

    /** Administrador: Dañado → En mantenimiento → Disponible. */
    public void avanzarEstado(Equipo e, Usuario quien) throws ReglaException, SQLException {
        if (!quien.tiene("administrador")) {
            throw new ReglaException("Solo el administrador cambia el estado del equipo.");
        }
        switch (e.estado()) {
            case "Dañado" -> equipo.cambiarEstado(e.id(), "En mantenimiento");
            case "En mantenimiento" -> equipo.cambiarEstado(e.id(), "Disponible");
            default -> throw new ReglaException("Ese equipo ya está disponible.");
        }
    }
}
