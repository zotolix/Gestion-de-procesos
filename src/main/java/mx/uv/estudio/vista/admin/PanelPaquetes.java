package mx.uv.estudio.vista.admin;

import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import mx.uv.estudio.modelo.Paquete;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.PaqueteService;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import net.miginfocom.swing.MigLayout;

/** HU11 · CRUD de paquetes: el administrador consulta, agrega, cambia y elimina paquetes y precios. */
public class PanelPaquetes extends Pantalla {

    private final PaqueteService servicio = new PaqueteService();

    public PanelPaquetes(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Paquetes y precios"));
        add(Ui.tenue("<html>Precios finales con IVA incluido. Las sesiones ya agendadas conservan el precio y la duración con que se agendaron.<br>"
                + "Un paquete que ya tiene sesiones no se borra: solo deja de aparecer en el catálogo.</html>"),
                "gapbottom 10");
        JPanel fila = new JPanel(new MigLayout("insets 0, gap 14, wrap 2", "[grow,fill,sg][grow,fill,sg]"));
        for (Paquete p : servicio.todos()) {
            fila.add(tarjeta(p), "top");
        }
        fila.add(tarjeta(null), "top");
        add(fila);
    }

    private JPanel tarjeta(Paquete p) {
        JPanel t = Ui.tarjeta("wrap 2, fillx", "[][grow,fill]");
        t.add(Ui.subtitulo(p == null ? "Paquete nuevo" : p.nombre()), "span 2");
        JTextField nombre = new JTextField(p == null ? "" : p.nombre());
        JTextField incluye = new JTextField(p == null ? "" : p.incluye());
        JTextField precio = new JTextField(p == null ? "" : p.precio().toPlainString());
        boolean sinLimite = p != null && p.duracionMin() == null;
        JSpinner duracion = new JSpinner(new SpinnerNumberModel(p == null || sinLimite ? 60 : p.duracionMin(), 15, 720, 15));
        JCheckBox chkSinLimite = new JCheckBox("Sin límite (bloquea el día)", sinLimite);
        duracion.setEnabled(!sinLimite);
        chkSinLimite.addActionListener(e -> duracion.setEnabled(!chkSinLimite.isSelected()));
        t.add(new JLabel("Nombre"));
        t.add(nombre);
        t.add(new JLabel("Incluye"));
        t.add(incluye);
        t.add(new JLabel("Precio"));
        t.add(precio);
        t.add(new JLabel("Duración (min)"));
        t.add(duracion);
        t.add(new JLabel());
        t.add(chkSinLimite);
        int id = p == null ? 0 : p.id();
        t.add(Accion.boton(this, p == null ? "Agregar paquete" : "Guardar cambios", true,
                () -> servicio.guardar(id, nombre.getText(), incluye.getText(), Ui.leerMonto(precio.getText()),
                        chkSinLimite.isSelected() ? null : (Integer) duracion.getValue()),
                () -> {
                    Ui.info(this, "Paquete guardado.");
                    refrescar();
                }), "span 2, split 2, growx 0, gaptop 6");
        if (p != null) {
            JButton eliminar = new JButton("Eliminar");
            eliminar.setForeground(Ui.MAL);
            eliminar.addActionListener(e -> eliminar(p));
            t.add(eliminar, "gaptop 6");
        }
        return t;
    }

    private void eliminar(Paquete p) {
        int r = JOptionPane.showConfirmDialog(this, "¿Eliminar \"" + p.nombre() + "\" del catálogo?", "Eliminar paquete",
                JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            boolean borrado = servicio.eliminar(p.id());
            Ui.info(this, borrado ? "Paquete eliminado."
                    : "El paquete ya tiene sesiones agendadas, así que se retiró del catálogo sin borrar esas sesiones.");
            refrescar();
        } catch (SQLException ex) {
            Ui.error(this, "No se pudo eliminar: " + ex.getMessage());
        }
    }
}
