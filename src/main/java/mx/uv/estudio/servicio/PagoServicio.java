package mx.uv.estudio.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import mx.uv.estudio.dao.PagoDao;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.util.Db;

public class PagoServicio {

    private final PagoDao pagoDao = new PagoDao();

    /** Registra el pago del 50% restante para liquidar el trabajo (HU21) */
    public int registrarLiquidacion(int sesionId, BigDecimal montoRestante, String metodoPago) throws SQLException, ReglaException {
        if (montoRestante == null || montoRestante.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaException("El monto a liquidar debe ser mayor a cero.");
        }

        try (Connection c = Db.con()) {
            // Inserta el pago usando tu PagoDao existente con el concepto fijo "Liquidación"
            return pagoDao.insertar(c, sesionId, "Liquidación", montoRestante, metodoPago, LocalDate.now());
        }
    }
}