package mx.uv.estudio.servicio;

import java.sql.SQLException;
import java.util.Optional;
import mx.uv.estudio.dao.UsuarioDao;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Usuario;
import org.mindrot.jbcrypt.BCrypt;

/** HU16 · Iniciar sesión por rol. */
public class AuthService {

    private final UsuarioDao usuarios = new UsuarioDao();

    public Usuario entrar(String correo, String contrasena) throws ReglaException, SQLException {
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isEmpty()) {
            throw new ReglaException("Escribe tu correo y tu contraseña.");
        }
        Optional<UsuarioDao.ConHash> u = usuarios.porCorreo(correo.trim());
        if (u.isEmpty() || !BCrypt.checkpw(contrasena, u.get().hash())) {
            throw new ReglaException("Correo o contraseña incorrectos.");
        }
        if (u.get().usuario().roles().isEmpty()) {
            throw new ReglaException("Tu cuenta no tiene roles asignados. Pide al administrador que te asigne uno.");
        }
        return u.get().usuario();
    }
}
