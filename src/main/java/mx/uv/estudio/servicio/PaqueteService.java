package mx.uv.estudio.servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.uv.estudio.dao.PaqueteDao;
import mx.uv.estudio.modelo.Paquete;
import mx.uv.estudio.modelo.ReglaException;

/** HU11 · Catálogo de paquetes. El administrador lo mantiene: alta, consulta, cambios y baja (CRUD). */
public class PaqueteService {

    private final PaqueteDao paquetes = new PaqueteDao();

    public List<Paquete> todos() throws SQLException {
        return paquetes.todos();
    }

    /** id 0 = paquete nuevo. duracionMin null = sin límite. */
    public void guardar(int id, String nombre, String incluye, BigDecimal precio, Integer duracionMin) throws ReglaException, SQLException {
        List<String> err = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) {
            err.add("Escribe el nombre del paquete.");
        }
        if (incluye == null || incluye.isBlank()) {
            err.add("Escribe qué incluye el paquete.");
        }
        if (precio == null || precio.signum() <= 0) {
            err.add("Escribe un precio mayor que cero.");
        }
        if (duracionMin != null && duracionMin <= 0) {
            err.add("La duración debe ser mayor que cero.");
        }
        if (!err.isEmpty()) {
            throw new ReglaException(err);
        }
        if (id == 0) {
            paquetes.crear(nombre.trim(), incluye.trim(), precio, duracionMin);
        } else {
            paquetes.actualizar(id, nombre.trim(), incluye.trim(), precio, duracionMin);
        }
    }

    /**
     * Quita el paquete del catálogo. Si ya tiene sesiones agendadas no se borra de verdad
     * (esas sesiones conservan su paquete): solo deja de aparecer. Devuelve true si se borró del todo.
     */
    public boolean eliminar(int id) throws SQLException {
        if (paquetes.tieneSesiones(id)) {
            paquetes.retirar(id);
            return false;
        }
        paquetes.borrar(id);
        return true;
    }
}
