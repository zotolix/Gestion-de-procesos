package mx.uv.estudio.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import mx.uv.estudio.dao.NotificacionDao;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Db;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.admin.PanelAvisos;
import mx.uv.estudio.vista.admin.PanelPaquetes;
import mx.uv.estudio.vista.admin.PanelRevision;
import mx.uv.estudio.vista.admin.PanelUsuarios;
import mx.uv.estudio.vista.cajero.PanelCatalogo;
import mx.uv.estudio.vista.cajero.PanelTurno;
import mx.uv.estudio.vista.comun.PanelAgendar;
import mx.uv.estudio.vista.editor.PanelAvisosEditor;
import mx.uv.estudio.vista.editor.PanelProyectosEditor;
import mx.uv.estudio.vista.comun.PanelInventario;
import mx.uv.estudio.vista.fotografo.PanelCalendario;
import mx.uv.estudio.vista.fotografo.PanelSubirFotos;
import net.miginfocom.swing.MigLayout;

/**
 * Ventana principal. A la izquierda, el menú con las pantallas de todos los roles del usuario
 * (una persona puede tener varios, pregunta 1). Al centro, la pantalla elegida.
 */
public class MainFrame extends JFrame {

    private final Usuario usuario;
    private final CardLayout cards = new CardLayout();
    private final JPanel centro = new JPanel(cards);
    private final JPanel menu = new JPanel(new MigLayout("insets 14 12, wrap 1, gap 4", "[200!,fill]"));
    private final ButtonGroup grupo = new ButtonGroup();
    private final Map<String, Pantalla> pantallas = new LinkedHashMap<>();
    private final Map<String, JToggleButton> botones = new LinkedHashMap<>();
    private final Map<String, String> titulos = new LinkedHashMap<>();
    /** Pantalla que muestra el número de avisos sin leer del rol. */
    private final Map<String, String> avisosDe = Map.of("avisos", "administrador", "editor", "editor");
    private final NotificacionDao avisos = new NotificacionDao();
    private String actual;

    public MainFrame(Usuario usuario) {
        super("Sistema del estudio · " + usuario.nombre());
        this.usuario = usuario;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sprint 1 y 2: HU16, HU01, HU04, HU11, HU15 (Sprint 1), HU02, HU05, HU12 (Sprint 2), HU03, HU06, HU07, HU09 (Sprint 3); HU17 a HU20 (administrador)
        if (usuario.tiene("cajero")) {
            seccion("Cajero");
            agregar("catalogo", "Catálogo de paquetes", new PanelCatalogo(usuario, this));
            agregar("agendar", "Agendar y anticipo", new PanelAgendar(usuario, this));
            agregar("turno", "Fondo de caja", new PanelTurno(usuario, this));
        }
        if (usuario.tiene("fotografo")) {
            seccion("Fotógrafo");
            agregar("calendario", "Mi calendario", new PanelCalendario(usuario, this));
            agregar("fotos", "Subir fotos", new PanelSubirFotos(usuario, this));
        }
        if (usuario.tiene("editor")) {
            seccion("Editor");
            agregar("proyectos", "Proyectos", new PanelProyectosEditor(usuario, this));
            agregar("editor", "Avisos", new PanelAvisosEditor(usuario, this));
        }
        if (usuario.tiene("administrador")) {
            seccion("Administrador");
            agregar("avisos", "Avisos", new PanelAvisos(usuario, this));
            agregar("revision", "Revisión de proyectos", new PanelRevision(usuario, this));
            agregar("usuarios", "Usuarios y roles", new PanelUsuarios(usuario, this));
            agregar("paquetes", "Paquetes y precios", new PanelPaquetes(usuario, this));
        }
        if (usuario.tiene("fotografo") || usuario.tiene("administrador")) {
            // el inventario lo usan el fotógrafo (reporta daños) y el administrador (agrega y atiende equipo)
            agregar("inventario", "Inventario de equipo", new PanelInventario(usuario, this));
        }

        add(barraSuperior(), BorderLayout.NORTH);
        JScrollPane spMenu = new JScrollPane(menu);
        spMenu.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, java.awt.Color.LIGHT_GRAY));
        add(spMenu, BorderLayout.WEST);
        add(centro, BorderLayout.CENTER);
        add(pie(), BorderLayout.SOUTH);
        mostrar(pantallas.keySet().iterator().next());
    }

    private void seccion(String rol) {
        JLabel l = Ui.tenue(rol.toUpperCase());
        l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));
        menu.add(l, menu.getComponentCount() == 0 ? "" : "gaptop 12");
    }

    private void agregar(String clave, String titulo, Pantalla p) {
        pantallas.put(clave, p);
        titulos.put(clave, titulo);
        JScrollPane sp = new JScrollPane(p);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        centro.add(sp, clave);
        JToggleButton b = new JToggleButton(titulo);
        b.setHorizontalAlignment(JToggleButton.LEFT);
        b.addActionListener(e -> mostrar(clave));
        grupo.add(b);
        botones.put(clave, b);
        menu.add(b, "h 34!");
    }

    private JPanel barraSuperior() {
        JPanel barra = new JPanel(new MigLayout("insets 10 18, fillx", "[]push[]12[]"));
        JLabel marca = new JLabel("Sistema del estudio");
        marca.setFont(marca.getFont().deriveFont(Font.BOLD, 15f));
        marca.setForeground(Ui.ACENTO);
        barra.add(marca);
        barra.add(new JLabel(usuario.nombre() + " · " + usuario.rolesLegibles()));
        JButton salir = new JButton("Cerrar sesión");
        salir.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        barra.add(salir);
        JPanel norte = new JPanel(new BorderLayout());
        norte.add(barra, BorderLayout.CENTER);
        norte.add(new JSeparator(), BorderLayout.SOUTH);
        return norte;
    }

    private JPanel pie() {
        JPanel pie = new JPanel(new MigLayout("insets 6 18, fillx", "[]push[]"));
        pie.add(Ui.tenue("Proyecto escolar · Sprint 1, 2 y 3 + HU17 a HU20"));
        JButton reset = new JButton("Restablecer datos de ejemplo");
        reset.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Se borrarán todos los cambios y se cargarán los datos de ejemplo.",
                    "Restablecer datos", JOptionPane.OK_CANCEL_OPTION);
            if (r == JOptionPane.OK_OPTION) {
                Db.restablecer();
                pantallas.get(actual).refrescar();
            }
        });
        pie.add(reset);
        return pie;
    }

    public void mostrar(String clave) {
        actual = clave;
        cards.show(centro, clave);
        botones.get(clave).setSelected(true);
        pantallas.get(clave).refrescar();
    }

    /** Pone el número de avisos sin leer junto a la pantalla de avisos de cada rol. */
    public void actualizarMenu() {
        for (var e : avisosDe.entrySet()) {
            JToggleButton b = botones.get(e.getKey());
            if (b == null) {
                continue;
            }
            try {
                long n = avisos.deRol(e.getValue()).stream().filter(a -> !a.leida()).count();
                b.setText(titulos.get(e.getKey()) + (n > 0 ? "  (" + n + ")" : ""));
            } catch (SQLException ex) {
                b.setText(titulos.get(e.getKey()));
            }
        }
    }
}
