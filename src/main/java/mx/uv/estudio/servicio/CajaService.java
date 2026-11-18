package mx.uv.estudio.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.uv.estudio.dao.PagoDao;
import mx.uv.estudio.dao.SesionDao;
import mx.uv.estudio.dao.TurnoDao;
import mx.uv.estudio.modelo.Pago;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Turno;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Ui;

/**
 * HU12 · Fondo de caja al abrir el turno, control de cobros, historial de pagos pendientes
 * y finiquitados, y liquidación de saldos de clientes.
 */
public class CajaService {

    private final TurnoDao turnos = new TurnoDao();
    private final SesionDao sesiones = new SesionDao();
    private final PagoDao pagos = new PagoDao();

    public Optional<Turno> turnoDeHoy() throws SQLException {
        return turnos.deFecha(LocalDate.now());
    }

    public void abrir(BigDecimal fondo, Usuario cajero) throws ReglaException, SQLException {
        if (!cajero.tiene("cajero")) {
            throw new ReglaException("Solo el cajero abre el turno.");
        }
        Optional<Turno> t = turnoDeHoy();
        if (t.isPresent()) {
            throw new ReglaException("Ya hay un turno hoy, abierto por " + t.get().cajero() + ".");
        }
        if (fondo == null || fondo.signum() < 0) {
            throw new ReglaException("Escribe el monto del fondo en números (puede ser 0).");
        }
        turnos.abrir(LocalDate.now(), cajero.id(), fondo, LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
    }

    /**
     * Registra un pago para finiquitar o abonar a una sesión con saldo pendiente.
     * Si la sesión ya está liquidada/finiquitada, bloquea la operación y lanza ReglaException.
     */
    public Pago liquidar(int sesionId, BigDecimal monto, String metodo, Usuario cajero) throws ReglaException, SQLException {
        if (cajero == null || !cajero.tiene("cajero")) {
            throw new ReglaException("Solo el cajero puede registrar cobros o liquidar pagos.");
        }
        Sesion ses = sesiones.porId(sesionId)
                .orElseThrow(() -> new ReglaException("No se encontró la sesión con ID #" + sesionId + "."));

        // Validación bug: Detecta si el pago ya está finiquitado y bloquea el guardado duplicado
        if (ses.liquidada()) {
            throw new ReglaException("El cliente " + ses.clienteNombre() + " (Sesión #" + ses.id() + ") ya tiene su pago finiquitado. No se permite registrar un pago duplicado.");
        }

        List<String> errores = new ArrayList<>();
        if (monto == null || monto.signum() <= 0) {
            errores.add("Escribe un monto válido a pagar (mayor a 0).");
        } else {
            monto = monto.setScale(2, RoundingMode.HALF_UP);
            if (monto.compareTo(ses.saldo()) > 0) {
                errores.add("El monto a cobrar (" + Ui.dinero(monto) + ") no puede ser mayor al saldo pendiente (" + Ui.dinero(ses.saldo()) + ").");
            }
        }
        if (metodo == null || metodo.isBlank()) {
            errores.add("Elige una forma de pago válida.");
        }
        if (!errores.isEmpty()) {
            throw new ReglaException(errores);
        }

        String concepto = monto.compareTo(ses.saldo()) >= 0 ? "Liquidación" : "Abono";
        LocalDate hoy = LocalDate.now();
        int pagoId = pagos.insertar(sesionId, concepto, monto, metodo, hoy);
        return new Pago(pagoId, sesionId, concepto, monto, metodo, hoy);
    }

    /** Obtiene el historial de pagos de una sesión. */
    public List<Pago> pagosDeSesion(int sesionId) throws SQLException {
        return pagos.deSesion(sesionId);
    }

    /**
     * Devuelve las sesiones filtradas por estado (TODOS, PENDIENTES, FINIQUITADOS)
     * y por término de búsqueda en tiempo real (por nombre de cliente o ID).
     */
    public List<Sesion> obtenerSesiones(String filtroEstado, String busqueda) throws SQLException {
        List<Sesion> todas = sesiones.todas();
        String estadoNorm = (filtroEstado == null) ? "TODOS" : filtroEstado.toUpperCase().trim();
        String raw = (busqueda == null) ? "" : busqueda.trim();
        String q = normalizar(raw);
        String idDigitos = raw.replaceAll("[^0-9]", "");
        boolean esBusquedaId = raw.startsWith("#") || (raw.matches("^\\d+$") && raw.length() <= 5);

        List<Sesion> filtradas = new ArrayList<>();
        for (Sesion s : todas) {
            // Filtro por estado
            boolean pasaEstado = switch (estadoNorm) {
                case "PENDIENTES" -> !s.liquidada();
                case "FINIQUITADOS" -> s.liquidada();
                default -> true; // TODOS
            };
            if (!pasaEstado) {
                continue;
            }

            // Filtro por búsqueda (Nombre o ID)
            if (!q.isEmpty()) {
                boolean coincideId = (!idDigitos.isEmpty() && String.valueOf(s.id()).equals(idDigitos))
                        || String.valueOf(s.id()).equalsIgnoreCase(q)
                        || ("#" + s.id()).equalsIgnoreCase(q);
                if (esBusquedaId) {
                    if (!coincideId) {
                        continue;
                    }
                } else {
                    boolean coincideNombre = normalizar(s.clienteNombre()).contains(q);
                    boolean coincideTel = s.clienteTelefono() != null
                            && idDigitos.length() >= 4
                            && s.clienteTelefono().replaceAll("[^0-9]", "").contains(idDigitos);
                    if (!coincideNombre && !coincideId && !coincideTel) {
                        continue;
                    }
                }
            }
            filtradas.add(s);
        }
        return filtradas;
    }

    private static String normalizar(String t) {
        if (t == null) {
            return "";
        }
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}
