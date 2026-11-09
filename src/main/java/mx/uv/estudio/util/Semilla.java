package mx.uv.estudio.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Datos de ejemplo para las demos. Los paquetes y precios son los que dio Gonzalo;
 * las personas, clientes y equipo son inventados. Las fechas son relativas a hoy.
 * Contraseña de todas las cuentas: 1234
 */
final class Semilla {

    private Semilla() { }

    static void cargar(Connection c) throws SQLException {
        LocalDate hoy = LocalDate.now();
        String hash = BCrypt.hashpw("1234", BCrypt.gensalt(10));

        // usuarios: 1 Ana, 2 Bruno, 3 Carla, 4 Diego, 5 Elena
        String u = "INSERT INTO usuario (nombre, correo, hash) VALUES (?,?,?)";
        insertar(c, u, "Ana Torres", "ana@estudio.mx", hash);
        insertar(c, u, "Bruno Díaz", "bruno@estudio.mx", hash);
        insertar(c, u, "Carla Ruiz", "carla@estudio.mx", hash);
        insertar(c, u, "Diego Mora", "diego@estudio.mx", hash);
        insertar(c, u, "Elena Vega", "elena@estudio.mx", hash);
        String r = "INSERT INTO usuario_rol (usuario_id, rol) VALUES (?,?)";
        insertar(c, r, 1, "cajero");
        insertar(c, r, 2, "fotografo");
        insertar(c, r, 3, "fotografo");
        insertar(c, r, 3, "editor");          // ejemplo de una persona con dos roles
        insertar(c, r, 4, "editor");
        insertar(c, r, 5, "administrador");

        // paquetes de Gonzalo: 1 Básico, 2 Estándar, 3 Premium
        String p = "INSERT INTO paquete (nombre, incluye, precio, duracion_min) VALUES (?,?,?,?)";
        insertar(c, p, "Paquete Básico", "1 hora · 10 fotos digitales", new BigDecimal("800"), 60);
        insertar(c, p, "Paquete Estándar", "2 horas · 25 fotos digitales · 1 álbum impreso", new BigDecimal("1800"), 120);
        insertar(c, p, "Paquete Premium", "Sesión sin límite · todas las fotos editadas · álbum de lujo · ampliación", new BigDecimal("3500"), null);

        // equipo: 1-7
        String e = "INSERT INTO equipo (nombre, numero_serie, categoria, estado, reporte_texto, reporte_por, reporte_fecha) VALUES (?,?,?,?,?,?,?)";
        insertar(c, e, "Canon EOS R6", "CN-R6-0012", "Cámara", "Disponible", null, null, null);
        insertar(c, e, "Canon EOS R6", "CN-R6-0047", "Cámara", "Disponible", null, null, null);
        insertar(c, e, "Lente 50 mm f/1.8", "LN-50-3301", "Lente", "Disponible", null, null, null);
        insertar(c, e, "Flash Godox V1", "GX-V1-8820", "Accesorio", "Disponible", null, null, null);
        insertar(c, e, "Tripié", "TR-0005", "Accesorio", "Disponible", null, null, null);
        insertar(c, e, "Batería LP-E6", "BT-E6-0003", "Accesorio", "Dañado", "Ya no carga.", "Bruno Díaz", d(hoy, -1));
        insertar(c, e, "Lente 24-70 mm f/2.8", "LN-2470-118", "Lente", "En mantenimiento", "Se traba el zoom.", "Carla Ruiz", d(hoy, -6));

        // sesiones: 1-7 (precio y duración copiados del paquete)
        String s = "INSERT INTO sesion (fecha, hora, fotografo_id, paquete_id, precio, duracion_min, cliente_nombre, cliente_telefono, cliente_correo, anotaciones) VALUES (?,?,?,?,?,?,?,?,?,?)";
        insertar(c, s, d(hoy, -5), t("12:00"), 2, 1, new BigDecimal("800"), 60, "Jorge Salas", "228 123 4567", "jorge.salas@correo.mx", "Retrato para currículum, fondo gris.");
        insertar(c, s, d(hoy, -3), t("10:00"), 2, 2, new BigDecimal("1800"), 120, "Laura Méndez", "228 555 0192", "laura.mendez@correo.mx", "Fotos en exterior, estilo natural.");
        insertar(c, s, d(hoy, -2), t("16:00"), 3, 1, new BigDecimal("800"), 60, "Familia Ortiz", "228 777 3010", "ortiz.familia@correo.mx", "Vienen 5 personas y un perro.");
        insertar(c, s, d(hoy, 0), t("11:00"), 2, 3, new BigDecimal("3500"), null, "Valeria Cruz", "228 400 2211", "vale.cruz@correo.mx", "XV años en jardín. Cambio de vestido a la mitad.");
        insertar(c, s, d(hoy, 1), t("17:00"), 3, 2, new BigDecimal("1800"), 120, "Mario Ríos", "228 901 4455", "mario.rios@correo.mx", "");
        insertar(c, s, d(hoy, 3), t("10:00"), 2, 1, new BigDecimal("800"), 60, "Familia Luna", "228 320 8890", "luna.fam@correo.mx", "");
        insertar(c, s, d(hoy, -8), t("15:00"), 3, 2, new BigDecimal("1800"), 120, "Sofía Paredes", "228 610 7788", "sofia.paredes@correo.mx", "Sesión de embarazo.");

        String se = "INSERT INTO sesion_equipo (sesion_id, equipo_id) VALUES (?,?)";
        insertar(c, se, 1, 1); insertar(c, se, 1, 3);
        insertar(c, se, 2, 1);
        insertar(c, se, 3, 2); insertar(c, se, 3, 5);
        insertar(c, se, 4, 1); insertar(c, se, 4, 4);
        insertar(c, se, 5, 2);

        // pagos: anticipos de 50 % o más, y un pago total
        String pg = "INSERT INTO pago (sesion_id, concepto, monto, metodo, fecha) VALUES (?,?,?,?,?)";
        insertar(c, pg, 1, "Anticipo", new BigDecimal("400"), "Efectivo", d(hoy, -9));
        insertar(c, pg, 2, "Anticipo", new BigDecimal("900"), "Tarjeta", d(hoy, -6));
        insertar(c, pg, 3, "Anticipo", new BigDecimal("400"), "Transferencia", d(hoy, -4));
        insertar(c, pg, 4, "Anticipo", new BigDecimal("1750"), "Tarjeta", d(hoy, -2));
        insertar(c, pg, 5, "Anticipo", new BigDecimal("1200"), "Efectivo", d(hoy, 0));      // más del 50 %
        insertar(c, pg, 6, "Liquidación", new BigDecimal("800"), "Transferencia", d(hoy, -1));   // pagó el total al agendar
        insertar(c, pg, 7, "Anticipo", new BigDecimal("900"), "Efectivo", d(hoy, -12));
        insertar(c, pg, 7, "Liquidación", new BigDecimal("900"), "Tarjeta", d(hoy, -1));

        // proyectos de edición (para la revisión del administrador, HU20): sesiones 1, 2 y 3 ya se realizaron; la 7 queda sin proyecto para probar la subida de fotos (HU03)
        String pr = "INSERT INTO proyecto (sesion_id, num_fotos, estado, aprobado, comentario) VALUES (?,?,?,?,?)";
        insertar(c, pr, 1, 10, "Revisión", false, null);
        insertar(c, pr, 2, 25, "Revisión", false, null);
        insertar(c, pr, 3, 10, "En proceso", false, "Subir un poco la exposición en las fotos de exterior.");

        String nt = "INSERT INTO notificacion (rol, texto, fecha, leida) VALUES (?,?,?,?)";
        insertar(c, nt, "administrador", "Equipo dañado: Batería LP-E6 (BT-E6-0003) · Ya no carga. · Bruno Díaz", d(hoy, -1), false);
    }

    private static Date d(LocalDate hoy, int dias) {
        return Date.valueOf(hoy.plusDays(dias));
    }

    private static Time t(String hhmm) {
        return Time.valueOf(LocalTime.parse(hhmm));
    }

    private static void insertar(Connection c, String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
            }
            ps.executeUpdate();
        }
    }
}
