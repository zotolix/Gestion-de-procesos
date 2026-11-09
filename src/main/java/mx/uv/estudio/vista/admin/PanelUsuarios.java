package mx.uv.estudio.vista.admin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.UsuarioService;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import net.miginfocom.swing.MigLayout;

/** HU17 · El administrador crea usuarios y asigna o cambia sus roles (una persona puede tener varios). */
public class PanelUsuarios extends Pantalla {

    private final UsuarioService servicio = new UsuarioService();

    public PanelUsuarios(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Usuarios y roles"));
        add(Ui.tenue("<html>Cada empleado entra con su correo y ve los módulos de todos sus roles. "
                + "Los correos no se repiten.</html>"), "gapbottom 10");
        JPanel lista = Ui.tarjeta("wrap 2, fillx, gap 14 10", "[grow,fill][]");
        lista.add(Ui.subtitulo("Usuarios"), "span 2");
        for (Usuario u : servicio.todos()) {
            lista.add(fila(u), "span 2, growx");
        }
        JPanel cuerpo = new JPanel(new MigLayout("insets 0, gap 16", "[grow,fill][320!]"));
        cuerpo.add(lista, "top");
        cuerpo.add(formulario(), "top");
        add(cuerpo);
    }

    private JPanel fila(Usuario u) {
        JPanel f = new JPanel(new MigLayout("insets 4 0, fillx, gap 8", "[grow,fill][][]"));
        f.add(new JLabel("<html><b>" + Ui.escapar(u.nombre()) + "</b><br><font color='#59606C'>"
                + Ui.escapar(u.correo()) + "</font></html>"));
        Map<String, JCheckBox> chks = casillas(u.roles());
        JPanel roles = new JPanel(new MigLayout("insets 0, gap 6"));
        chks.values().forEach(c -> roles.add(c));
        f.add(roles);
        f.add(Accion.boton(this, "Guardar roles", false,
                () -> servicio.cambiarRoles(u, elegidos(chks), usuario),
                () -> {
                    Ui.info(this, "Roles de " + u.nombre() + " actualizados. Los verá la próxima vez que inicie sesión.");
                    refrescar();
                }));
        return f;
    }

    private JPanel formulario() {
        JPanel f = Ui.tarjeta("wrap 1, fillx", "[grow,fill]");
        f.add(Ui.subtitulo("Usuario nuevo"));
        JTextField nombre = new JTextField();
        JTextField correo = new JTextField();
        JPasswordField clave = new JPasswordField();
        Map<String, JCheckBox> chks = casillas(List.of());
        f.add(new JLabel("Nombre"));
        f.add(nombre);
        f.add(new JLabel("Correo"));
        f.add(correo);
        f.add(new JLabel("Contraseña"));
        f.add(clave);
        f.add(new JLabel("Roles"));
        chks.values().forEach(c -> f.add(c));
        f.add(Accion.boton(this, "Crear usuario", true,
                () -> servicio.crear(nombre.getText(), correo.getText(), new String(clave.getPassword()),
                        elegidos(chks), usuario),
                () -> {
                    Ui.info(this, "Usuario creado.");
                    refrescar();
                }), "growx 0, gaptop 6, h 36!");
        return f;
    }

    private static Map<String, JCheckBox> casillas(List<String> marcados) {
        Map<String, JCheckBox> m = new LinkedHashMap<>();
        for (String rol : Usuario.ROLES) {
            m.put(rol, new JCheckBox(Usuario.legible(rol), marcados.contains(rol)));
        }
        return m;
    }

    private static List<String> elegidos(Map<String, JCheckBox> chks) {
        List<String> r = new ArrayList<>();
        chks.forEach((rol, c) -> {
            if (c.isSelected()) {
                r.add(rol);
            }
        });
        return r;
    }
}
