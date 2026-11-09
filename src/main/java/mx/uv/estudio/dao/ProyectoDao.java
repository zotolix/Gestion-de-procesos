package mx.uv.estudio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.uv.estudio.modelo.Proyecto;
import mx.uv.estudio.util.Db;

public class ProyectoDao {

    private static final String BASE = "SELECT p.id, p.sesion_id, s.cliente_nombre, s.fecha, p.num_fotos, p.estado, "
            + "p.aprobado, p.comentario FROM proyecto p JOIN sesion s ON s.id = p.sesion_id ";

    public List<Proyecto> todos() throws SQLException {
        return lista(BASE + "ORDER BY p.id");
    }

    public List<Proyecto> deFotografo(int fotografoId) throws SQLException {
        return lista(BASE + "WHERE s.fotografo_id = ? ORDER BY p.id", fotografoId);
    }

    public Optional<Proyecto> porId(int id) throws SQLException {
        List<Proyecto> l = lista(BASE + "WHERE p.id = ?", id);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    public Optional<Proyecto> porSesion(int sesionId) throws SQLException {
        List<Proyecto> l = lista(BASE + "WHERE p.sesion_id = ?", sesionId);
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
    }

    /** Proyecto nuevo en Pendiente (HU03). Usa la conexión recibida para guardarse junto con sus fotos. */
    public int crear(Connection c, int sesionId, int numFotos) throws SQLException {
        return Sql.insertar(c, "INSERT INTO proyecto (sesion_id, num_fotos, estado) VALUES (?,?,?)",
                sesionId, numFotos, "Pendiente");
    }

    public void sumarFotos(Connection c, int id, int n) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE proyecto SET num_fotos = num_fotos + ? WHERE id = ?")) {
            Sql.poner(ps, n, id);
            ps.executeUpdate();
        }
    }

    /** Etiqueta de avance (HU07). Al pasar a Revisión se borra el comentario anterior del administrador. */
    public void cambiarEstado(int id, String estado) throws SQLException {
        if ("Revisión".equals(estado)) {
            Sql.ejecutar("UPDATE proyecto SET estado = ?, comentario = NULL WHERE id = ?", estado, id);
        } else {
            Sql.ejecutar("UPDATE proyecto SET estado = ? WHERE id = ?", estado, id);
        }
    }

    /** Proyectos en Revisión que el administrador todavía no aprueba ni regresa. */
    public List<Proyecto> porRevisar() throws SQLException {
        return lista(BASE + "WHERE p.estado = ? AND p.aprobado = 0 ORDER BY p.id", "Revisión");
    }

    /** Proyectos en Revisión que el administrador ya aprobó (esperan al editor). */
    public List<Proyecto> aprobadosEnEspera() throws SQLException {
        return lista(BASE + "WHERE p.estado = ? AND p.aprobado = 1 ORDER BY p.id", "Revisión");
    }

    public void aprobar(int id) throws SQLException {
        Sql.ejecutar("UPDATE proyecto SET aprobado = 1, comentario = NULL WHERE id = ?", id);
    }

    public void regresar(int id, String comentario) throws SQLException {
        Sql.ejecutar("UPDATE proyecto SET estado = ?, aprobado = 0, comentario = ? WHERE id = ?",
                "En proceso", comentario, id);
    }

    private List<Proyecto> lista(String sql, Object... params) throws SQLException {
        List<Proyecto> r = new ArrayList<>();
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(sql)) {
            Sql.poner(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.add(new Proyecto(rs.getInt("id"), rs.getInt("sesion_id"), rs.getString("cliente_nombre"),
                            rs.getObject("fecha", LocalDate.class), rs.getInt("num_fotos"), rs.getString("estado"),
                            rs.getBoolean("aprobado"), rs.getString("comentario")));
                }
            }
        }
        return r;
    }
}
