package mx.uv.estudio.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Db;

public class UsuarioDao {

    /** Usuario y su hash de contraseña, para validar el inicio de sesión. */
    public record ConHash(Usuario usuario, String hash) { }

    public Optional<ConHash> porCorreo(String correo) throws SQLException {
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(
                "SELECT id, nombre, correo, hash FROM usuario WHERE LOWER(correo) = LOWER(?)")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                int id = rs.getInt("id");
                return Optional.of(new ConHash(new Usuario(id, rs.getString("nombre"), rs.getString("correo"), roles(c, id)),
                        rs.getString("hash")));
            }
        }
    }

    public List<Usuario> todos() throws SQLException {
        return lista("SELECT id, nombre, correo FROM usuario ORDER BY id");
    }

    public List<Usuario> conRol(String rol) throws SQLException {
        return lista("SELECT u.id, u.nombre, u.correo FROM usuario u JOIN usuario_rol r ON r.usuario_id = u.id "
                + "WHERE r.rol = ? ORDER BY u.nombre", rol);
    }

    public boolean existeCorreo(String correo) throws SQLException {
        return porCorreo(correo).isPresent();
    }

    public void crear(String nombre, String correo, String hash, List<String> roles) throws SQLException {
        try (Connection c = Db.con()) {
            c.setAutoCommit(false);
            try {
                int id = Sql.insertar(c, "INSERT INTO usuario (nombre, correo, hash) VALUES (?,?,?)", nombre, correo, hash);
                guardarRoles(c, id, roles);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void cambiarRoles(int usuarioId, List<String> roles) throws SQLException {
        try (Connection c = Db.con()) {
            c.setAutoCommit(false);
            try (PreparedStatement del = c.prepareStatement("DELETE FROM usuario_rol WHERE usuario_id = ?")) {
                del.setInt(1, usuarioId);
                del.executeUpdate();
                guardarRoles(c, usuarioId, roles);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void guardarRoles(Connection c, int usuarioId, List<String> roles) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO usuario_rol (usuario_id, rol) VALUES (?,?)")) {
            for (String r : roles) {
                ps.setInt(1, usuarioId);
                ps.setString(2, r);
                ps.executeUpdate();
            }
        }
    }

    private List<String> roles(Connection c, int usuarioId) throws SQLException {
        List<String> r = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT rol FROM usuario_rol WHERE usuario_id = ?")) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.add(rs.getString(1));
                }
            }
        }
        // mismo orden siempre: cajero, fotógrafo, editor, administrador
        r.sort((a, b) -> Usuario.ROLES.indexOf(a) - Usuario.ROLES.indexOf(b));
        return r;
    }

    private List<Usuario> lista(String sql, Object... params) throws SQLException {
        List<Usuario> r = new ArrayList<>();
        try (Connection c = Db.con(); PreparedStatement ps = c.prepareStatement(sql)) {
            Sql.poner(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    r.add(new Usuario(id, rs.getString("nombre"), rs.getString("correo"), roles(c, id)));
                }
            }
        }
        return r;
    }
}
