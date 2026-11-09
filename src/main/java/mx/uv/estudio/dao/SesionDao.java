package mx.uv.estudio.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.util.Db;

public class SesionDao {

    private static final String SELECT =
        "SELECT s.id, s.fecha, s.hora, s.fotografo_id, u.nombre AS fotografo, s.paquete_id, p.nombre AS paquete, "
      + "s.precio, s.duracion_min, s.cliente_nombre, s.cliente_telefono, s.cliente_correo, s.anotaciones, s.estado, "
      + "(SELECT COALESCE(SUM(g.monto), 0) FROM pago g WHERE g.sesion_id = s.id) AS pagado "
      + "FROM sesion s JOIN usuario u ON u.id = s.fotografo_id JOIN paquete p ON p.id = s.paquete_id ";

    public List<Sesion> todas() throws SQLException {
        return lista(SELECT + "ORDER BY s.fecha DESC, s.hora DESC");
    }

    public List<Sesion> deFotografo(int fotografoId) throws SQLException {
        return lista(SELECT + "WHERE s.fotografo_id = ? AND s.estado = 'Agendada' ORDER BY s.fecha, s.hora", fotografoId);
    }

    /** Sesiones agendadas de un fotógrafo en una fecha (para revisar cruces). */
    public List<Sesion> agendadasDeFotografo(int fotografoId, LocalDate fecha) throws SQLException {
        return lista(SELECT + "WHERE s.fotografo_id = ? AND s.fecha = ? AND s.estado = 'Agendada'", fotografoId, fecha);
    }

    /** Sesiones agendadas en una fecha que usan cierto equipo (para revisar cruces del equipo). */
    public List<Sesion> agendadasConEquipo(int equipoId, LocalDate fecha) throws SQLException {
        return lista(SELECT + "WHERE s.fecha = ? AND s.estado = 'Agendada' "
                + "AND s.id IN (SELECT sesion_id FROM sesion_equipo WHERE equipo_id = ?)", fecha, equipoId);
    }

    public Optional<Sesion> porId(int id) throws SQLException {
        List<Sesion> l = lista(SELECT + "WHERE s.id = ?", id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    public int insertar(Connection c, LocalDate fecha, LocalTime hora, int fotografoId, int paqueteId, BigDecimal precio,
                        Integer duracionMin, String nombre, String telefono, String correo) throws SQLException {
        return Sql.insertar(c, "INSERT INTO sesion (fecha, hora, fotografo_id, paquete_id, precio, duracion_min, "
                + "cliente_nombre, cliente_telefono, cliente_correo) VALUES (?,?,?,?,?,?,?,?,?)",
                fecha, hora, fotografoId, paqueteId, precio, duracionMin, nombre, telefono, correo);
    }

    public void guardarAnotaciones(int sesionId, String texto) throws SQLException {
        Sql.ejecutar("UPDATE sesion SET anotaciones = ? WHERE id = ?", texto, sesionId);
    }

    public List<Integer> equipoDe(int sesionId) throws SQLException {
        List<Integer> r = new ArrayList<>();
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement("SELECT equipo_id FROM sesion_equipo WHERE sesion_id = ?")) {
            ps.setInt(1, sesionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.add(rs.getInt(1));
                }
            }
        }
        return r;
    }

    /** Reemplaza el equipo de una sesión. Si se pasa una conexión, se usa (para transacciones). */
    public void asignarEquipo(Connection c, int sesionId, List<Integer> equipoIds) throws SQLException {
        try (PreparedStatement del = c.prepareStatement("DELETE FROM sesion_equipo WHERE sesion_id = ?");
             PreparedStatement ins = c.prepareStatement("INSERT INTO sesion_equipo (sesion_id, equipo_id) VALUES (?,?)")) {
            del.setInt(1, sesionId);
            del.executeUpdate();
            for (int e : equipoIds) {
                ins.setInt(1, sesionId);
                ins.setInt(2, e);
                ins.executeUpdate();
            }
        }
    }

    public void asignarEquipo(int sesionId, List<Integer> equipoIds) throws SQLException {
        try (Connection c = Db.con()) {
            c.setAutoCommit(false);
            try {
                asignarEquipo(c, sesionId, equipoIds);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private List<Sesion> lista(String sql, Object... params) throws SQLException {
        List<Sesion> r = new ArrayList<>();
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(sql)) {
            Sql.poner(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int d = rs.getInt("duracion_min");
                    Integer dur = rs.wasNull() ? null : d;
                    r.add(new Sesion(rs.getInt("id"), rs.getObject("fecha", LocalDate.class), rs.getObject("hora", LocalTime.class),
                            rs.getInt("fotografo_id"), rs.getString("fotografo"), rs.getInt("paquete_id"), rs.getString("paquete"),
                            rs.getBigDecimal("precio"), dur, rs.getString("cliente_nombre"), rs.getString("cliente_telefono"),
                            rs.getString("cliente_correo"), rs.getString("anotaciones"), rs.getString("estado"), rs.getBigDecimal("pagado")));
                }
            }
        }
        return r;
    }
}
