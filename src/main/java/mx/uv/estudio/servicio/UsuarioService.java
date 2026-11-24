package mx.uv.estudio.servicio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.uv.estudio.dao.UsuarioDao;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

/** HU17 · El administrador crea usuarios y les asigna uno o más roles; después puede cambiarlos. */
public class UsuarioService {

    private final UsuarioDao usuarios = new UsuarioDao();

    public List<Usuario> todos() throws SQLException {
        return usuarios.todos();
    }

    public void crear(String nombre, String correo, String contrasena, List<String> roles, Usuario quien)
            throws ReglaException, SQLException {
        soloAdmin(quien);
        List<String> err = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) {
            err.add("Escribe el nombre.");
        } else if (!nombre.trim().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'\\-]+$") || !nombre.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ].*")) {
            err.add("El nombre solo debe contener letras.");
        } else if (usuarios.existeNombre(nombre.trim())) {
            err.add("El nombre de usuario ya está en uso.");
        }
        if (correo == null || correo.isBlank()) {
            err.add("Escribe el correo.");
        } else if (!correo.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            err.add("Escribe un correo válido.");
        } else if (usuarios.existeCorreo(correo.trim())) {
            err.add("El correo electrónico ya está registrado.");
        }
        if (contrasena == null || contrasena.length() < 4) {
            err.add("La contraseña debe tener al menos 4 caracteres.");
        }
        if (roles == null || roles.isEmpty()) {
            err.add("Elige al menos un rol.");
        }
        if (!err.isEmpty()) {
            throw new ReglaException(err);
        }
        try {
            usuarios.crear(nombre.trim(), correo.trim(), BCrypt.hashpw(contrasena, BCrypt.gensalt(10)), roles);
        } catch (SQLException e) {
            // Evita mostrar errores crudos de SQL (p. ej. violación de unicidad por carrera).
            if (usuarios.existeCorreo(correo.trim())) {
                throw new ReglaException("El correo electrónico ya está registrado.");
            }
            if (usuarios.existeNombre(nombre.trim())) {
                throw new ReglaException("El nombre de usuario ya está en uso.");
            }
            throw new ReglaException("No se pudo guardar el usuario.");
        }
    }

    public void cambiarRoles(Usuario u, List<String> roles, Usuario quien) throws ReglaException, SQLException {
        soloAdmin(quien);
        if (roles == null || roles.isEmpty()) {
            throw new ReglaException("Cada usuario necesita al menos un rol.");
        }
        if (u.tiene("administrador") && !roles.contains("administrador")
                && usuarios.conRol("administrador").size() <= 1) {
            throw new ReglaException("No se puede quitar el rol Administrador al único administrador del sistema.");
        }
        usuarios.cambiarRoles(u.id(), roles);
    }

    private void soloAdmin(Usuario quien) throws ReglaException {
        if (!quien.tiene("administrador")) {
            throw new ReglaException("Solo el administrador crea usuarios y asigna roles.");
        }
    }
}
