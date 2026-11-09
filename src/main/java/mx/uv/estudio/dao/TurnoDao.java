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
import mx.uv.estudio.modelo.Turno;
import mx.uv.estudio.util.Db;

public class TurnoDao {

    private static final String SELECT = "SELECT t.id, t.fecha, u.nombre, t.fondo, t.hora_apertura "
            + "FROM turno t JOIN usuario u ON u.id = t.usuario_id ";

    public Optional<Turno> deFecha(LocalDate fecha) throws SQLException {
        List<Turno> r = new ArrayList<>();
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(SELECT + "WHERE t.fecha = ?")) {
            ps.setObject(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.add(new Turno(rs.getInt(1), rs.getObject(2, LocalDate.class), rs.getString(3), rs.getBigDecimal(4),
                            rs.getObject(5, LocalTime.class)));
                }
            }
        }
        return r.isEmpty() ? Optional.empty() : Optional.of(r.get(0));
    }

    public void abrir(LocalDate fecha, int usuarioId, BigDecimal fondo, LocalTime hora) throws SQLException {
        Sql.ejecutar("INSERT INTO turno (fecha, usuario_id, fondo, hora_apertura) VALUES (?,?,?,?)", fecha, usuarioId, fondo, hora);
    }
}
