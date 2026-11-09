package mx.uv.estudio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.uv.estudio.modelo.Equipo;
import mx.uv.estudio.util.Db;

public class EquipoDao {

    private static final String SELECT =
        "SELECT id, nombre, numero_serie, categoria, estado, reporte_texto, reporte_por, reporte_fecha FROM equipo ";

    public List<Equipo> todos() throws SQLException {
        return lista(SELECT + "ORDER BY categoria, nombre, numero_serie");
    }

    public List<Equipo> noDisponibles() throws SQLException {
        return lista(SELECT + "WHERE estado <> 'Disponible' ORDER BY reporte_fecha DESC");
    }

    public Optional<Equipo> porId(int id) throws SQLException {
        List<Equipo> l = lista(SELECT + "WHERE id = ?", id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    public void insertar(String nombre, String serie, String categoria) throws SQLException {
        Sql.ejecutar("INSERT INTO equipo (nombre, numero_serie, categoria, estado) VALUES (?, ?, ?, 'Disponible')", nombre, serie, categoria);
    }

    /** Marca el equipo como dañado y lo quita de las sesiones de hoy en adelante. */
    public void reportarDanio(int equipoId, String texto, String por, LocalDate fecha) throws SQLException {
        try (Connection c = Db.con()) {
            c.setAutoCommit(false);
            try (PreparedStatement up = c.prepareStatement(
                    "UPDATE equipo SET estado = 'Dañado', reporte_texto = ?, reporte_por = ?, reporte_fecha = ? WHERE id = ?");
                 PreparedStatement del = c.prepareStatement(
                    "DELETE FROM sesion_equipo WHERE equipo_id = ? AND sesion_id IN (SELECT id FROM sesion WHERE fecha >= ?)")) {
                Sql.poner(up, texto, por, fecha, equipoId);
                up.executeUpdate();
                Sql.poner(del, equipoId, fecha);
                del.executeUpdate();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** estado: En mantenimiento o Disponible (lo cambia el administrador). Al quedar disponible se borra el reporte. */
    public void cambiarEstado(int equipoId, String estado) throws SQLException {
        if ("Disponible".equals(estado)) {
            Sql.ejecutar("UPDATE equipo SET estado = ?, reporte_texto = NULL, reporte_por = NULL, reporte_fecha = NULL WHERE id = ?", estado, equipoId);
        } else {
            Sql.ejecutar("UPDATE equipo SET estado = ? WHERE id = ?", estado, equipoId);
        }
    }

    private List<Equipo> lista(String sql, Object... params) throws SQLException {
        List<Equipo> r = new ArrayList<>();
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(sql)) {
            Sql.poner(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.add(new Equipo(rs.getInt("id"), rs.getString("nombre"), rs.getString("numero_serie"), rs.getString("categoria"),
                            rs.getString("estado"), rs.getString("reporte_texto"), rs.getString("reporte_por"),
                            rs.getObject("reporte_fecha", LocalDate.class)));
                }
            }
        }
        return r;
    }
}
