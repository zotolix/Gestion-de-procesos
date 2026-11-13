package mx.uv.estudio.vista.cajero;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.PagoServicio;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;

/** HU21 · El cajero cobra el resto del pago para liquidar el trabajo antes de la entrega. */
public class PanelLiquidacion extends Pantalla {

    private int sesionId; // El ID de la sesión/trabajo a liquidar
    private BigDecimal montoRestante; // El 50% pendiente calculado

    public PanelLiquidacion(Usuario usuario, MainFrame main, int sesionId, BigDecimal montoRestante) {
        super(usuario, main);
        this.sesionId = sesionId;
        this.montoRestante = montoRestante;
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Liquidar trabajo"));
        add(Ui.tenue("Cobra el 50 % restante en efectivo, tarjeta o transferencia. Se genera el recibo con concepto Liquidación."), "gapbottom 10");
        add(formularioLiquidacion(), "w 420!");
    }

    private JPanel formularioLiquidacion() {
        JPanel p = Ui.tarjeta("wrap 1, fillx", "[grow,fill]");
        p.add(Ui.subtitulo("Resumen del cobro"));
        
        p.add(new JLabel("Monto restante a pagar (50%):"));
        JLabel lblMonto = Ui.titulo(Ui.dinero(montoRestante));
        lblMonto.setForeground(Ui.ACENTO);
        p.add(lblMonto);

        p.add(new JLabel("Método de pago (Efectivo, Tarjeta, Transferencia)"));
        
        // Selector con las opciones exigidas por la HU21
        JComboBox<String> comboMetodo = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia"});
        p.add(comboMetodo);

        JButton btnLiquidar = Ui.primario("Cobrar y liquidar trabajo");
        btnLiquidar.addActionListener(e -> {
            try {
                String metodoSeleccionado = (String) comboMetodo.getSelectedItem();
                new PagoServicio().registrarLiquidacion(sesionId, montoRestante, metodoSeleccionado);
                
                javax.swing.JOptionPane.showMessageDialog(this, "¡Trabajo liquidado con éxito! Recibo enviado.");
                refrescar();
            } catch (ReglaException ex) {
                Ui.error(this, ex.getMessage());
            } catch (SQLException ex) {
                Ui.error(this, "No se pudo registrar la liquidación: " + ex.getMessage());
            }
        });

        p.add(btnLiquidar, "growx 0, gaptop 6, h 38!");
        return p;
    }
}