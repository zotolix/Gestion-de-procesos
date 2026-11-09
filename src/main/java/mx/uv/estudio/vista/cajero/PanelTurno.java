package mx.uv.estudio.vista.cajero;

import java.sql.SQLException;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Turno;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.CajaService;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;

/** HU12 · El cajero registra el fondo de caja al iniciar su turno. Hay un turno al día y un solo cajero a la vez. */
public class PanelTurno extends Pantalla {

    private final CajaService caja = new CajaService();

    public PanelTurno(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Fondo de caja"));
        add(Ui.tenue("Un turno por día y un solo cajero a la vez. Al abrir el turno se registra el fondo."), "gapbottom 10");
        Optional<Turno> t = caja.turnoDeHoy();
        if (t.isEmpty()) {
            add(abrir(), "w 420!");
        } else {
            add(resumen(t.get()), "w 420!");
        }
    }

    private JPanel abrir() {
        JPanel p = Ui.tarjeta("wrap 1, fillx", "[grow,fill]");
        p.add(Ui.subtitulo("Abrir turno de hoy"));
        p.add(new JLabel("Fondo de caja (efectivo con el que empiezas)"));
        JTextField monto = new JTextField();
        p.add(monto);
        JButton b = Ui.primario("Abrir turno");
        b.addActionListener(e -> {
            try {
                caja.abrir(Ui.leerMonto(monto.getText()), usuario);
                refrescar();
            } catch (ReglaException ex) {
                Ui.error(this, ex.getMessage());
            } catch (SQLException ex) {
                Ui.error(this, "No se pudo guardar: " + ex.getMessage());
            }
        });
        p.add(b, "growx 0, gaptop 6, h 38!");
        return p;
    }

    private JPanel resumen(Turno t) {
        JPanel p = Ui.tarjeta("wrap 2, fillx", "[grow][right]");
        p.add(Ui.subtitulo("Turno de hoy"), "span 2");
        p.add(Ui.chip("Abierto", Ui.OK), "span 2, gapbottom 8");
        p.add(Ui.tenue("Cajero"));
        p.add(new JLabel(t.cajero()));
        p.add(Ui.tenue("Hora de apertura"));
        p.add(new JLabel(Ui.hora(t.apertura())));
        p.add(Ui.subtitulo("Fondo de caja"), "gaptop 6");
        p.add(Ui.subtitulo(Ui.dinero(t.fondo())));
        return p;
    }
}
