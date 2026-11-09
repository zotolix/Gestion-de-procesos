package mx.uv.estudio.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Conexión única a la base de datos.
 * Por defecto usa H2 en la carpeta ./datos, en modo compatible con SQL Server,
 * para que el proyecto corra sin instalar nada. Para usar SQL Server real
 * cambia URL, USUARIO y CLAVE (ver README.md).
 */
public final class Db {

    private static final String URL = "jdbc:h2:./datos/estudio;MODE=MSSQLServer;AUTO_SERVER=TRUE";
    private static final String USUARIO = "sa";
    private static final String CLAVE = "";

    private static final String[] TABLAS = {
        // en orden para no romper llaves foráneas; fondo_caja y catalogo son de versiones anteriores
        "notificacion", "foto", "proyecto", "sesion_equipo", "equipo", "turno", "fondo_caja",
        "pago", "sesion", "paquete", "catalogo", "usuario_rol", "usuario"
    };

    private Db() { }

    public static Connection con() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /** Crea las tablas y carga los datos de ejemplo la primera vez. */
    public static void iniciar() {
        boolean existen;
        try (Connection c = con()) {
            existen = existenTablas(c);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo abrir la base de datos: " + e.getMessage(), e);
        }
        if (!existen) {
            restablecer(); // también borra tablas de una versión anterior, si las hay
        }
    }

    /** Borra todo y vuelve a cargar los datos de ejemplo. */
    public static void restablecer() {
        try (Connection c = con(); Statement st = c.createStatement()) {
            for (String t : TABLAS) {
                st.execute("DROP TABLE IF EXISTS " + t);
            }
            crearTablas(c);
            Semilla.cargar(c);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron restablecer los datos: " + e.getMessage(), e);
        }
    }

    private static boolean existenTablas(Connection c) {
        try (Statement st = c.createStatement()) {
            st.executeQuery("SELECT COUNT(*) FROM usuario_rol").close();
            st.executeQuery("SELECT activo FROM paquete").close(); // falla si la base es de una versión anterior
            st.executeQuery("SELECT aprobado FROM proyecto").close(); // la tabla proyecto llegó con HU20
            st.executeQuery("SELECT calidad FROM foto").close();      // la tabla foto llegó con el Sprint 3
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static void crearTablas(Connection c) throws SQLException {
        String sql;
        try (var in = Db.class.getResourceAsStream("/sql/schema.sql");
             var br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            sql = br.lines()
                    .map(l -> l.contains("--") ? l.substring(0, l.indexOf("--")) : l)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new SQLException("No se encontró sql/schema.sql", e);
        }
        try (Statement st = c.createStatement()) {
            for (String sentencia : sql.split(";")) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia);
                }
            }
        }
    }
}
