package mx.uv.estudio.vista.cajero;

import java.awt.BorderLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mx.uv.estudio.modelo.Pago;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.CajaService;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import mx.uv.estudio.vista.comun.Campos;
import net.miginfocom.swing.MigLayout;

/**
 * HU (Historial): Sección aparte en caja para visualizar y filtrar el registro de
 * "Pagos Pendientes" y "Pagos Finiquitados" sin perder el historial de clientes que ya liquidaron,
 * y HU (Buscador): búsqueda y filtrado en tiempo real por nombre o ID.
 */
public class PanelHistorialPagos extends Pantalla {

    private final CajaService caja = new CajaService();
    private final JTextField txtBuscar = new JTextField();
    private final JPanel panelLista = new JPanel(new MigLayout("insets 0, fillx, wrap 1, gap 10", "[grow,fill]"));
    private String filtroActual = "TODOS"; // "TODOS", "PENDIENTES", "FINIQUITADOS"

    public PanelHistorialPagos(Usuario usuario, MainFrame main) {
        super(usuario, main);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { cargarLista(); }
            @Override public void removeUpdate(DocumentEvent e) { cargarLista(); }
            @Override public void changedUpdate(DocumentEvent e) { cargarLista(); }
        });
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Historial y control de pagos"));
        add(Ui.tenue("Monitorea los cobros de clientes, filtra pagos pendientes y finiquitados sin perder el historial maestro."), "gapbottom 12");

        // Barra de herramientas: Búsqueda en tiempo real y selector de filtros
        JPanel barra = Ui.tarjeta("wrap 2, fillx, gap 12 10", "[grow,fill][right]");
        barra.add(Ui.subtitulo("Buscador de clientes"), "span 2");
        barra.add(Ui.tenue("Escribe el nombre del cliente o ID de sesión (#) para filtrar en tiempo real:"), "span 2");
        barra.add(txtBuscar, "growx");

        // Botones de filtro de estado
        JPanel filtros = new JPanel(new MigLayout("insets 0, gap 6"));
        ButtonGroup grupoFiltros = new ButtonGroup();

        JToggleButton btnTodos = new JToggleButton("Todos");
        JToggleButton btnPendientes = new JToggleButton("Pagos Pendientes");
        JToggleButton btnFiniquitados = new JToggleButton("Pagos Finiquitados");

        grupoFiltros.add(btnTodos);
        grupoFiltros.add(btnPendientes);
        grupoFiltros.add(btnFiniquitados);

        btnTodos.setSelected("TODOS".equals(filtroActual));
        btnPendientes.setSelected("PENDIENTES".equals(filtroActual));
        btnFiniquitados.setSelected("FINIQUITADOS".equals(filtroActual));

        btnTodos.addActionListener(e -> { filtroActual = "TODOS"; cargarLista(); });
        btnPendientes.addActionListener(e -> { filtroActual = "PENDIENTES"; cargarLista(); });
        btnFiniquitados.addActionListener(e -> { filtroActual = "FINIQUITADOS"; cargarLista(); });

        filtros.add(btnTodos);
        filtros.add(btnPendientes);
        filtros.add(btnFiniquitados);
        barra.add(filtros, "right");

        add(barra);
        add(panelLista, "gaptop 10");

        cargarLista();
    }

    private void cargarLista() {
        panelLista.removeAll();
        try {
            List<Sesion> todas = caja.obtenerSesiones("TODOS", "");
            long totalPendientes = todas.stream().filter(s -> !s.liquidada()).count();
            long totalFiniquitados = todas.stream().filter(Sesion::liquidada).count();

            // Resumen superior de contadores
            JPanel contadores = new JPanel(new MigLayout("insets 0, gap 12"));
            contadores.add(Ui.chip("Total registros: " + todas.size(), Ui.BLANCO));
            contadores.add(Ui.chip("Pagos pendientes: " + totalPendientes, Ui.AVISO));
            contadores.add(Ui.chip("Pagos finiquitados: " + totalFiniquitados, Ui.OK));
            panelLista.add(contadores, "gapbottom 6");

            List<Sesion> lista = caja.obtenerSesiones(filtroActual, txtBuscar.getText());
            if (lista.isEmpty()) {
                JPanel vacio = Ui.tarjeta("wrap 1, fillx");
                vacio.add(Ui.tenue("No se encontraron clientes o sesiones para el criterio de búsqueda y filtro seleccionado."));
                panelLista.add(vacio);
            } else {
                for (Sesion s : lista) {
                    panelLista.add(tarjetaCliente(s));
                }
            }
        } catch (SQLException e) {
            JLabel err = new JLabel("Error al consultar el historial de pagos: " + e.getMessage());
            err.setForeground(Ui.MAL);
            panelLista.add(err);
        }
        panelLista.revalidate();
        panelLista.repaint();
    }

    private JPanel tarjetaCliente(Sesion s) {
        JPanel t = Ui.tarjeta("wrap 4, fillx, gap 16 8", "[grow,fill,180:260:][140:180:][140:200:][right]");

        // Columna 1: Folio, Cliente y Contacto
        JPanel colCliente = new JPanel(new MigLayout("insets 0, wrap 1, gap 2"));
        JLabel lblNombre = new JLabel("#" + s.id() + " · " + s.clienteNombre());
        lblNombre.setFont(lblNombre.getFont().deriveFont(Font.BOLD, 16f));
        lblNombre.setForeground(Ui.BLANCO);
        colCliente.add(lblNombre);
        colCliente.add(Ui.tenue("Tel: " + s.clienteTelefono()));
        colCliente.add(Ui.tenue("Correo: " + s.clienteCorreo()));
        t.add(colCliente);

        // Columna 2: Detalles de la sesión
        JPanel colSesion = new JPanel(new MigLayout("insets 0, wrap 1, gap 2"));
        colSesion.add(new JLabel("<html><b>" + Ui.escapar(s.paquete()) + "</b></html>"));
        colSesion.add(Ui.tenue("Fecha: " + Ui.fecha(s.fecha()) + " " + Ui.hora(s.hora())));
        colSesion.add(Ui.tenue("Fotógrafo: " + s.fotografo()));
        t.add(colSesion);

        // Columna 3: Montos y Saldo
        JPanel colMontos = new JPanel(new MigLayout("insets 0, wrap 1, gap 2"));
        colMontos.add(new JLabel("Total: " + Ui.dinero(s.precio())));
        colMontos.add(Ui.tenue("Abonado: " + Ui.dinero(s.pagado())));
        if (s.liquidada()) {
            colMontos.add(Ui.chip("Finiquitado", Ui.OK));
        } else {
            colMontos.add(Ui.chip("Saldo: " + Ui.dinero(s.saldo()), Ui.AVISO));
        }
        t.add(colMontos);

        // Columna 4: Acciones (Cobro y Recibos)
        JPanel colAcciones = new JPanel(new MigLayout("insets 0, wrap 1, gap 6"));
        if (!s.liquidada()) {
            JButton btnCobrar = Ui.primario("Cobrar saldo");
            btnCobrar.addActionListener(e -> cobrarSaldo(s));
            colAcciones.add(btnCobrar, "w 130!");
        } else {
            JLabel lblListo = Ui.chip("Liquidado", Ui.OK);
            colAcciones.add(lblListo, "center");
        }

        JButton btnRecibos = new JButton("Ver recibos");
        btnRecibos.addActionListener(e -> verRecibos(s));
        colAcciones.add(btnRecibos, "w 130!");

        t.add(colAcciones, "right");
        return t;
    }

    /** Permite finiquitar o abonar a un cliente con saldo pendiente. */
    private void cobrarSaldo(Sesion s) {
        // Validación Bug: Detectar si el pago ya está finiquitado y bloquear guardado duplicado
        if (s.liquidada()) {
            Ui.error(this, "El cliente " + s.clienteNombre() + " (Sesión #" + s.id() + ") ya tiene su pago finiquitado.\nNo se permite registrar un cobro duplicado.");
            return;
        }

        JTextField txtMontoCobro = new JTextField(s.saldo().toPlainString());
        Campos.soloDecimal(txtMontoCobro);
        JComboBox<String> cbMetodoCobro = new JComboBox<>(new String[] {"Efectivo", "Tarjeta", "Transferencia"});

        JPanel formCobro = new JPanel(new MigLayout("wrap 2, fillx, insets 10, gap 10", "[][grow,fill]"));
        formCobro.add(new JLabel("Cliente:"));
        formCobro.add(new JLabel("<b>" + Ui.escapar(s.clienteNombre()) + " (Sesión #" + s.id() + ")</b>"));
        formCobro.add(new JLabel("Paquete:"));
        formCobro.add(new JLabel(Ui.escapar(s.paquete())));
        formCobro.add(new JLabel("Precio total:"));
        formCobro.add(new JLabel(Ui.dinero(s.precio())));
        formCobro.add(new JLabel("Total abonado:"));
        formCobro.add(new JLabel(Ui.dinero(s.pagado())));
        formCobro.add(new JLabel("Saldo pendiente:"));
        formCobro.add(new JLabel(Ui.dinero(s.saldo())));
        formCobro.add(new JLabel("Monto a cobrar:"));
        formCobro.add(txtMontoCobro);
        formCobro.add(new JLabel("Forma de pago:"));
        formCobro.add(cbMetodoCobro);

        int res = JOptionPane.showConfirmDialog(this, formCobro, "Registrar cobro / Finiquitar saldo", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            BigDecimal monto = Ui.leerMonto(txtMontoCobro.getText());
            String metodo = (String) cbMetodoCobro.getSelectedItem();
            try {
                Pago pago = caja.liquidar(s.id(), monto, metodo, usuario);
                Ui.info(this, "<html><b>Pago registrado con éxito</b><br><br>"
                        + "Cliente: " + Ui.escapar(s.clienteNombre()) + "<br>"
                        + "Folio de recibo: " + pago.folio() + "<br>"
                        + "Concepto: " + pago.concepto() + "<br>"
                        + "Monto cobrado: " + Ui.dinero(pago.monto()) + " (" + pago.metodo() + ")<br>"
                        + (pago.monto().compareTo(s.saldo()) >= 0 ? "¡El pago ha quedado completamente finiquitado!" : "Abono registrado.")
                        + "</html>");
                // Mantiene el cliente en el historial maestro actualizando solo su estado a Finiquitado
                cargarLista();
            } catch (ReglaException ex) {
                Ui.error(this, ex.getMessage());
            } catch (SQLException ex) {
                Ui.error(this, "Error al procesar el cobro: " + ex.getMessage());
            }
        }
    }

    /** Muestra el desglose de todos los pagos registrados para la sesión seleccionada. */
    private void verRecibos(Sesion s) {
        try {
            List<Pago> pagos = caja.pagosDeSesion(s.id());
            JPanel p = new JPanel(new MigLayout("wrap 1, fillx, insets 10, gap 6", "[grow,fill]"));
            p.add(new JLabel("<html><b>Historial de recibos · " + Ui.escapar(s.clienteNombre()) + " (Sesión #" + s.id() + ")</b></html>"));
            p.add(Ui.tenue("Paquete: " + s.paquete() + " · Total: " + Ui.dinero(s.precio())), "gapbottom 8");

            if (pagos.isEmpty()) {
                p.add(Ui.tenue("No hay pagos registrados para esta sesión."));
            } else {
                for (Pago pg : pagos) {
                    JPanel item = Ui.tarjeta("wrap 2, fillx, insets 8", "[grow][right]");
                    item.add(new JLabel("Folio: #" + pg.folio() + " · " + pg.concepto()));
                    item.add(new JLabel("<b>" + Ui.dinero(pg.monto()) + "</b>"));
                    item.add(Ui.tenue("Fecha: " + Ui.fecha(pg.fecha()) + " · " + pg.metodo()), "span 2");
                    p.add(item);
                }
            }
            JOptionPane.showMessageDialog(this, p, "Recibos de la sesión #" + s.id(), JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException ex) {
            Ui.error(this, "No se pudieron obtener los recibos: " + ex.getMessage());
        }
    }
}
