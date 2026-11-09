package mx.uv.estudio.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import mx.uv.estudio.dao.PagoDao;
import mx.uv.estudio.dao.SesionDao;
import mx.uv.estudio.modelo.Paquete;
import mx.uv.estudio.modelo.Pago;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Db;
import mx.uv.estudio.util.Ui;

/**
 * HU15 · Agendar con anticipo (lo hace el cajero), HU04 · Anotaciones del cliente (las escribe el fotógrafo)
 * y búsqueda de clientes por nombre.
 */
public class AgendaService {

    /** El anticipo mínimo es el 50 % del precio. Se puede pagar más, hasta el total. */
    public static final BigDecimal PORCENTAJE_ANTICIPO = new BigDecimal("0.50");

    private final SesionDao sesiones = new SesionDao();
    private final PagoDao pagos = new PagoDao();
    private final Disponibilidad disp = new Disponibilidad();

    public record Solicitud(String nombre, String telefono, String correo, LocalDate fecha, LocalTime hora,
                            Usuario fotografo, Paquete paquete, BigDecimal monto, String metodo) { }

    /** Lo mínimo que hay que pagar al agendar: el 50 % del precio. */
    public static BigDecimal anticipoMinimo(BigDecimal precio) {
        return precio.multiply(PORCENTAJE_ANTICIPO).setScale(2, RoundingMode.HALF_UP);
    }

    public Pago agendar(Solicitud s, Usuario quien) throws ReglaException, SQLException {
        if (!quien.tiene("cajero")) {
            throw new ReglaException("Solo el cajero agenda sesiones.");
        }
        List<String> err = new ArrayList<>();
        if (s.nombre() == null || s.nombre().isBlank()) {
            err.add("Escribe el nombre del cliente.");
        }
        if (s.telefono() == null || !s.telefono().trim().matches("[0-9 ()+-]{8,}")) {
            err.add("Escribe un teléfono de al menos 8 dígitos.");
        }
        if (s.correo() == null || !s.correo().trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            err.add("Escribe un correo válido.");
        }
        if (s.paquete() == null) {
            err.add("Elige un paquete.");
        }
        validarFecha(s.fecha(), s.hora(), err);
        if (s.fotografo() == null) {
            err.add("Elige un fotógrafo. Si no aparece ninguno, no hay fotógrafos libres en ese horario.");
        }
        if (s.metodo() == null) {
            err.add("Elige la forma de pago.");
        }
        BigDecimal monto = s.monto() == null ? null : s.monto().setScale(2, RoundingMode.HALF_UP);
        if (s.paquete() != null) {
            if (monto == null || monto.signum() <= 0) {
                err.add("Escribe cuánto paga el cliente hoy.");
            } else if (monto.compareTo(anticipoMinimo(s.paquete().precio())) < 0) {
                err.add("El pago mínimo al agendar es el 50 % (" + Ui.dinero(anticipoMinimo(s.paquete().precio())) + ").");
            } else if (monto.compareTo(s.paquete().precio()) > 0) {
                err.add("El pago no puede ser mayor que el total del paquete (" + Ui.dinero(s.paquete().precio()) + ").");
            }
        }
        if (err.isEmpty()) {
            disp.choqueFotografo(s.fotografo().id(), s.fecha(), s.hora(), s.paquete().duracionMin(), 0).ifPresent(o ->
                    err.add(s.fotografo().nombre() + " ya tiene la sesión de " + o.clienteNombre() + " (" + o.horario() + ")."));
        }
        if (!err.isEmpty()) {
            throw new ReglaException(err);
        }
        // Si paga el total, el trabajo queda liquidado desde el primer pago.
        String concepto = monto.compareTo(s.paquete().precio()) >= 0 ? "Liquidación" : "Anticipo";
        LocalDate hoy = LocalDate.now();
        try (Connection c = Db.con()) {
            c.setAutoCommit(false);
            try {
                int sesionId = sesiones.insertar(c, s.fecha(), s.hora(), s.fotografo().id(), s.paquete().id(), s.paquete().precio(),
                        s.paquete().duracionMin(), s.nombre().trim(), s.telefono().trim(), s.correo().trim());
                int pagoId = pagos.insertar(c, sesionId, concepto, monto, s.metodo(), hoy);
                c.commit();
                return new Pago(pagoId, sesionId, concepto, monto, s.metodo(), hoy);
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void guardarAnotaciones(int sesionId, String texto) throws SQLException {
        sesiones.guardarAnotaciones(sesionId, texto == null ? "" : texto.trim());
    }

    /** Sesiones cuyo cliente coincide con el texto (sin importar mayúsculas ni acentos), de la más reciente a la más antigua. */
    public List<Sesion> buscarPorCliente(String texto) throws SQLException {
        String q = normalizar(texto);
        List<Sesion> r = new ArrayList<>();
        if (q.isEmpty()) {
            return r;
        }
        for (Sesion s : sesiones.todas()) {
            if (normalizar(s.clienteNombre()).contains(q)) {
                r.add(s);
            }
        }
        return r;
    }

    private static String normalizar(String t) {
        if (t == null) {
            return "";
        }
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    private void validarFecha(LocalDate fecha, LocalTime hora, List<String> err) {
        if (fecha == null || hora == null) {
            err.add("Elige fecha y hora.");
        } else if (fecha.atTime(hora).isBefore(LocalDateTime.now())) {
            err.add("Esa fecha y hora ya pasaron.");
        }
    }
}
