package mx.uv.estudio.vista.comun;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mx.uv.estudio.dao.SesionDao;
import mx.uv.estudio.modelo.Pago;
import mx.uv.estudio.modelo.Paquete;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.AgendaService;
import mx.uv.estudio.servicio.Disponibilidad;
import mx.uv.estudio.servicio.PaqueteService;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import net.miginfocom.swing.MigLayout;

/**
 * HU15 · Agendar una sesión y registrar el anticipo. Lo hace el cajero.
 * El pago mínimo es el 50 % del paquete; se puede pagar más o el total.
 * Solo aparecen los fotógrafos libres en ese horario. Arriba hay un buscador de clientes por nombre.
 */
public class PanelAgendar extends Pantalla {

    private static final String[] METODOS = {"Efectivo", "Tarjeta", "Transferencia"};
    private static final int MAX_RESULTADOS = 6;

    private final AgendaService agenda = new AgendaService();
    private final Disponibilidad disp = new Disponibilidad();
    private final JTextField txtBuscar = new JTextField();
    private final JPanel resultados = new JPanel(new MigLayout("insets 0, wrap 2, fillx, gap 4 10", "[grow,fill,10:10:][]"));
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JComboBox<Paquete> cbPaquete = new JComboBox<>();
    private final JSpinner spFecha = Campos.fecha(LocalDate.now());
    private final JSpinner spHora = Campos.hora(Campos.siguienteHora());
    private final JComboBox<Usuario> cbFotografo = new JComboBox<>();
    private final JLabel lblFotografos = Ui.tenue(" ");
    private final JComboBox<String> cbMetodo = new JComboBox<>(METODOS);
    private final JTextField txtMonto = new JTextField();
    private final JLabel lblErrores = new JLabel();
    private final JPanel resumen = Ui.tarjeta("wrap 2, fillx", "[grow][right]");
    private boolean armado;
    private boolean cargando;   // mientras se recarga el catálogo no se pisa el monto que ya escribieron

    public PanelAgendar(Usuario usuario, MainFrame main) {
        super(usuario, main);
        lblErrores.setForeground(Ui.MAL);
        cbPaquete.addActionListener(e -> {
            Paquete p = (Paquete) cbPaquete.getSelectedItem();
            if (p != null && !cargando) {
                txtMonto.setText(AgendaService.anticipoMinimo(p.precio()).toPlainString());
            }
            recalcular();
        });
        spFecha.addChangeListener(e -> recalcular());
        spHora.addChangeListener(e -> recalcular());
        txtMonto.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { actualizarResumen(); }
            @Override public void removeUpdate(DocumentEvent e) { actualizarResumen(); }
            @Override public void changedUpdate(DocumentEvent e) { actualizarResumen(); }
        });
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { buscar(); }
            @Override public void removeUpdate(DocumentEvent e) { buscar(); }
            @Override public void changedUpdate(DocumentEvent e) { buscar(); }
        });
    }

    /** El formulario se arma una sola vez para no perder lo que ya se escribió. */
    @Override
    protected void construir() throws SQLException {
        if (!armado) {
            armado = true;
            limpiar();
        } else {
            cargarPaquetes();
            recalcular();
        }
        add(Ui.titulo("Agendar sesión y registrar anticipo"));
        JPanel fila = new JPanel(new MigLayout("insets 0, gap 18", "[grow,fill][280!]"));
        fila.add(formulario(), "top");
        fila.add(resumen, "top");
        add(fila);
    }

    private void cargarPaquetes() throws SQLException {
        Object antes = cbPaquete.getSelectedItem();
        List<Paquete> lista = new PaqueteService().todos();
        cargando = true;
        try {
            cbPaquete.setModel(new DefaultComboBoxModel<>(lista.toArray(new Paquete[0])));
            if (antes instanceof Paquete p) {
                lista.stream().filter(x -> x.id() == p.id()).findFirst().ifPresent(cbPaquete::setSelectedItem);
            }
        } finally {
            cargando = false;
        }
    }

    private JPanel formulario() {
        JPanel f = Ui.tarjeta("wrap 3, fillx, gap 10 6", "[grow,fill,sg,100:220:][grow,fill,sg,100:220:][grow,fill,sg,100:220:]");
        f.add(lblErrores, "span 3");
        f.add(Ui.subtitulo("Buscar cliente por nombre"), "span 3");
        f.add(txtBuscar, "span 3");
        f.add(resultados, "span 3");
        f.add(Ui.subtitulo("Cliente"), "span 3, gaptop 10");
        f.add(new JLabel("Nombre"));
        f.add(new JLabel("Teléfono"));
        f.add(new JLabel("Correo"));
        f.add(txtNombre);
        f.add(txtTelefono);
        f.add(txtCorreo);
        f.add(Ui.subtitulo("Sesión"), "span 3, gaptop 10");
        f.add(new JLabel("Paquete"));
        f.add(new JLabel("Fecha"));
        f.add(new JLabel("Hora de inicio"));
        f.add(cbPaquete);
        f.add(spFecha);
        f.add(spHora);
        f.add(new JLabel("Fotógrafo"), "span 3");
        f.add(cbFotografo);
        f.add(lblFotografos, "span 2");
        f.add(Ui.tenue("El fotógrafo asigna el equipo desde su calendario."), "span 3");
        f.add(Ui.subtitulo("Pago"), "span 3, gaptop 10");
        f.add(new JLabel("Forma de pago"));
        f.add(new JLabel("Cuánto paga hoy"), "span 2");
        f.add(cbMetodo);
        f.add(txtMonto, "span 2");
        JPanel atajos = new JPanel(new MigLayout("insets 0, gap 6"));
        JButton b50 = new JButton("Mínimo 50 %");
        b50.addActionListener(e -> {
            Paquete p = (Paquete) cbPaquete.getSelectedItem();
            if (p != null) {
                txtMonto.setText(AgendaService.anticipoMinimo(p.precio()).toPlainString());
            }
        });
        JButton bTotal = new JButton("Pagar el total");
        bTotal.addActionListener(e -> {
            Paquete p = (Paquete) cbPaquete.getSelectedItem();
            if (p != null) {
                txtMonto.setText(p.precio().toPlainString());
            }
        });
        atajos.add(b50);
        atajos.add(bTotal);
        f.add(atajos, "span 3");
        f.add(Ui.tenue("Mínimo el 50 % del paquete. Puede pagar más o el total."), "span 3");
        JButton btn = Ui.primario("Agendar y registrar pago");
        btn.addActionListener(e -> agendar());
        f.add(btn, "span 3, growx 0, gaptop 12, h 40!");
        return f;
    }

    /** Muestra las sesiones cuyo cliente coincide con lo escrito y permite reusar sus datos. */
    private void buscar() {
        resultados.removeAll();
        String q = txtBuscar.getText().trim();
        if (!q.isEmpty()) {
            try {
                List<Sesion> r = agenda.buscarPorCliente(q);
                if (r.isEmpty()) {
                    resultados.add(Ui.tenue("Ningún cliente coincide con \"" + q + "\"."), "span 2");
                }
                for (Sesion s : r.stream().limit(MAX_RESULTADOS).toList()) {
                    resultados.add(new JLabel("<html><b>" + Ui.escapar(s.clienteNombre()) + "</b> · "
                            + Ui.escapar(s.clienteTelefono()) + " · " + Ui.escapar(s.clienteCorreo())
                            + "<br><font color='#59606C'>" + Ui.fecha(s.fecha()) + " " + Ui.hora(s.hora()) + " · "
                            + Ui.escapar(s.paquete()) + " · " + Ui.escapar(s.fotografo()) + " · " + s.estadoPago()
                            + "</font></html>"));
                    JButton usar = new JButton("Usar datos");
                    usar.addActionListener(e -> {
                        txtNombre.setText(s.clienteNombre());
                        txtTelefono.setText(s.clienteTelefono());
                        txtCorreo.setText(s.clienteCorreo());
                    });
                    resultados.add(usar);
                }
                if (r.size() > MAX_RESULTADOS) {
                    resultados.add(Ui.tenue("Hay " + (r.size() - MAX_RESULTADOS) + " más; escribe más del nombre para acotar."), "span 2");
                }
            } catch (SQLException e) {
                resultados.add(Ui.tenue("No se pudo buscar: " + e.getMessage()), "span 2");
            }
        }
        resultados.revalidate();
        resultados.repaint();
    }

    /** Vuelve a calcular los fotógrafos libres cuando cambia el paquete, la fecha o la hora. */
    private void recalcular() {
        Paquete p = (Paquete) cbPaquete.getSelectedItem();
        if (p == null) {
            return;
        }
        LocalDate f = Campos.leerFecha(spFecha);
        LocalTime h = Campos.leerHora(spHora);
        try {
            Usuario antes = (Usuario) cbFotografo.getSelectedItem();
            List<Usuario> libres = disp.fotografosLibres(f, h, p.duracionMin(), 0);
            cbFotografo.setModel(new DefaultComboBoxModel<>(libres.toArray(new Usuario[0])));
            if (antes != null) {
                libres.stream().filter(u -> u.id() == antes.id()).findFirst().ifPresent(cbFotografo::setSelectedItem);
            }
            lblFotografos.setText(libres.isEmpty() ? "No hay fotógrafos libres en ese horario."
                    : libres.size() + " fotógrafo(s) libre(s) en ese horario.");
            lblFotografos.setForeground(libres.isEmpty() ? Ui.MAL : Ui.TENUE);
        } catch (SQLException e) {
            lblErrores.setText("No se pudo revisar la disponibilidad: " + e.getMessage());
        }
        actualizarResumen();
    }

    private void actualizarResumen() {
        Paquete p = (Paquete) cbPaquete.getSelectedItem();
        resumen.removeAll();
        resumen.add(Ui.subtitulo("Resumen"), "span 2");
        if (p != null) {
            resumen.add(new JLabel("<html>" + Ui.escapar(p.nombre()) + "<br><font color='#59606C'>"
                    + Ui.escapar(p.incluye()) + "</font></html>"), "span 2, w 260!");
            resumen.add(Ui.tenue("Duración"), "gaptop 6");
            resumen.add(new JLabel(p.duracionLegible()));
            resumen.add(Ui.subtitulo("Total (IVA incluido)"), "gaptop 6");
            resumen.add(Ui.subtitulo(Ui.dinero(p.precio())));
            resumen.add(Ui.tenue("Pago mínimo (50 %)"));
            resumen.add(new JLabel(Ui.dinero(AgendaService.anticipoMinimo(p.precio()))));
            BigDecimal monto = Ui.leerMonto(txtMonto.getText());
            if (monto != null && monto.signum() > 0) {
                resumen.add(Ui.tenue("Paga hoy"), "gaptop 6");
                resumen.add(new JLabel(Ui.dinero(monto)));
                BigDecimal saldo = p.precio().subtract(monto);
                resumen.add(Ui.tenue("Saldo pendiente"));
                resumen.add(new JLabel(Ui.dinero(saldo.max(BigDecimal.ZERO))));
                if (monto.compareTo(AgendaService.anticipoMinimo(p.precio())) < 0 || monto.compareTo(p.precio()) > 0) {
                    resumen.add(Ui.chip("Monto fuera del rango permitido", Ui.MAL), "span 2, gaptop 6");
                } else if (saldo.signum() == 0) {
                    resumen.add(Ui.chip("Queda liquidado", Ui.OK), "span 2, gaptop 6");
                } else {
                    resumen.add(Ui.chip("Queda con anticipo", Ui.AVISO), "span 2, gaptop 6");
                }
            }
        }
        resumen.revalidate();
        resumen.repaint();
    }

    private void agendar() {
        var s = new AgendaService.Solicitud(txtNombre.getText(), txtTelefono.getText(), txtCorreo.getText(),
                Campos.leerFecha(spFecha), Campos.leerHora(spHora),
                (Usuario) cbFotografo.getSelectedItem(), (Paquete) cbPaquete.getSelectedItem(),
                Ui.leerMonto(txtMonto.getText()), (String) cbMetodo.getSelectedItem());
        try {
            Pago pago = agenda.agendar(s, usuario);
            lblErrores.setText("");
            Sesion ses = new SesionDao().porId(pago.sesionId()).orElseThrow();
            Ui.info(this, "<html><b>Sesión agendada</b><br><br>"
                    + Ui.escapar(ses.clienteNombre()) + " · " + Ui.escapar(ses.paquete()) + "<br>"
                    + Ui.fecha(ses.fecha()) + " · " + ses.horario() + " · " + Ui.escapar(ses.fotografo()) + "<br><br>"
                    + pago.concepto() + ": " + Ui.dinero(pago.monto()) + " (" + pago.metodo() + ")<br>"
                    + (ses.liquidada() ? "Pagado por completo." : "Saldo pendiente: " + Ui.dinero(ses.saldo())) + "</html>");
            limpiar();
        } catch (ReglaException e) {
            lblErrores.setText(Ui.lineas(e.mensajes()));
        } catch (SQLException e) {
            lblErrores.setText("No se pudo guardar: " + e.getMessage());
        }
        revalidate();
    }

    private void limpiar() {
        txtBuscar.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        cbMetodo.setSelectedIndex(0);
        try {
            cargarPaquetes();
        } catch (SQLException e) {
            lblErrores.setText("No se pudieron cargar los paquetes: " + e.getMessage());
        }
        Paquete p = (Paquete) cbPaquete.getSelectedItem();
        txtMonto.setText(p == null ? "" : AgendaService.anticipoMinimo(p.precio()).toPlainString());
        cbFotografo.setSelectedItem(null);
        recalcular();
    }
}
