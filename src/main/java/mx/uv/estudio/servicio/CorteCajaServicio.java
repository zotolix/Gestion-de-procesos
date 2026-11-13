package mx.uv.estudio.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import mx.uv.estudio.dao.TurnoDao;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Turno;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Db;

/** HU12 · Fondo de caja al abrir el turno. Hay un turno al día y un solo cajero a la vez. */
public class CorteCajaServicio {

    private final TurnoDao turnos = new TurnoDao();

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
            throw new ReglaException("Escribe el monto del fondo (puede ser 0).");
        }
        turnos.abrir(LocalDate.now(), cajero.id(), fondo, LocalTime.now().truncatedTo(ChronoUnit.MINUTES));
    }

    /** HU22 · Calcula el efectivo esperado sumando el fondo inicial y los cobros en efectivo del turno. */
    public BigDecimal calcularEfectivoEsperado(Turno turno) throws SQLException {
        BigDecimal fondoInicial = turno.fondo();
        BigDecimal cobrosEfectivo = BigDecimal.ZERO; // Reemplazar con la suma real del DAO de pagos si aplica
        
        return fondoInicial.add(cobrosEfectivo);
    }

    /** HU22 · Registra el corte de caja comparando el efectivo esperado con el contado físicamente. */
    public void registrarCorte(Turno turno, BigDecimal efectivoContado) throws ReglaException, SQLException {
        if (efectivoContado == null || efectivoContado.signum() < 0) {
            throw new ReglaException("Ingresa un monto válido para el efectivo contado.");
        }

        BigDecimal efectivoEsperado = calcularEfectivoEsperado(turno);
        BigDecimal diferencia = efectivoContado.subtract(efectivoEsperado);

        // Llamar a un método en TurnoDao o un CorteDao para guardar la información del corte en la BD:
    }
}