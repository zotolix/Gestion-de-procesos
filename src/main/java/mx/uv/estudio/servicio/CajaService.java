package mx.uv.estudio.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import mx.uv.estudio.dao.PagoDao;
import mx.uv.estudio.dao.TurnoDao;
import mx.uv.estudio.modelo.Pago;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Turno;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Db;

/** HU12 y HU22 · Gestión de turno, fondo de caja (mínimo $1,000) y corte de caja. */
public class CajaService {

    private static final BigDecimal FONDO_MINIMO = new BigDecimal("1000.00");

    private final TurnoDao turnos = new TurnoDao();
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
        if (fondo == null || fondo.compareTo(FONDO_MINIMO) < 0) {
            throw new ReglaException("El fondo de caja debe ser de al menos $1,000.00.");
        }
        turnos.abrir(LocalDate.now(), cajero.id(), fondo, LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
    }

    /** HU22 · Suma el fondo inicial del turno más los pagos cobrados en efectivo en la fecha del turno. */
    public BigDecimal calcularEfectivoEsperado(Turno turno) throws SQLException {
        if (turno == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal fondoInicial = turno.fondo() != null ? turno.fondo() : BigDecimal.ZERO;
        BigDecimal cobrosEfectivo = sumarCobrosEfectivoPorFecha(turno.fecha());
        return fondoInicial.add(cobrosEfectivo);
    }

    /** Suma los pagos con método "Efectivo" correspondientes a la fecha indicada. */
    public BigDecimal sumarCobrosEfectivoPorFecha(LocalDate fecha) throws SQLException {
        BigDecimal totalEfectivo = BigDecimal.ZERO;
        String sql = "SELECT id, sesion_id, concepto, monto, metodo, fecha FROM pago WHERE fecha = ? ORDER BY id";
        try (Connection c = Db.con(); var ps = c.prepareStatement(sql)) {
            ps.setObject(1, fecha);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    String metodo = rs.getString("metodo");
                    BigDecimal monto = rs.getBigDecimal("monto");
                    if (metodo != null && metodo.equalsIgnoreCase("Efectivo") && monto != null) {
                        totalEfectivo = totalEfectivo.add(monto);
                    }
                }
            }
        }
        return totalEfectivo;
    }

    /** HU22 · Valida el efectivo contado y calcula la diferencia contra el efectivo esperado. */
    public BigDecimal registrarCorte(Turno turno, BigDecimal efectivoContado) throws ReglaException, SQLException {
        if (turno == null) {
            throw new ReglaException("No hay un turno activo para realizar el corte.");
        }
        if (efectivoContado == null || efectivoContado.signum() < 0) {
            throw new ReglaException("Ingresa un monto válido para el efectivo contado.");
        }

        BigDecimal efectivoEsperado = calcularEfectivoEsperado(turno);
        BigDecimal diferencia = efectivoContado.subtract(efectivoEsperado);

        // TODO: Persistir el corte en base de datos cuando exista el método/tabla de cortes, por ejemplo:
        // corteDao.insertar(turno.id(), efectivoEsperado, efectivoContado, diferencia, LocalDate.now());

        return diferencia;
    }

    /** Consulta los pagos registrados en una sesión si se requiere revisar el detalle. */
    public List<Pago> pagosDeSesion(int sesionId) throws SQLException {
        return pagos.deSesion(sesionId);
    }
}
