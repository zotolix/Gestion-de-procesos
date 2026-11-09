package mx.uv.estudio.vista;

import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import mx.uv.estudio.dao.UsuarioDao;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.AuthService;
import mx.uv.estudio.util.Ui;
import net.miginfocom.swing.MigLayout;

/** HU16 · Iniciar sesión por rol. */
public class LoginFrame extends JFrame {

    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final JLabel lblError = new JLabel(" ");
    private final AuthService auth = new AuthService();

    public LoginFrame() {
        super("Sistema del estudio · Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new MigLayout("insets 32, wrap 1, fillx", "[420!]"));

        JLabel marca = Ui.tenue("SISTEMA DEL ESTUDIO");
        marca.setForeground(Ui.ACENTO);
        add(marca);
        add(Ui.titulo("Iniciar sesión"), "gapbottom 6");
        lblError.setForeground(Ui.MAL);
        add(lblError);
        add(new JLabel("Correo"));
        add(txtCorreo, "growx");
        add(new JLabel("Contraseña"), "gaptop 6");
        add(txtPass, "growx");
        JButton btnEntrar = Ui.primario("Entrar");
        btnEntrar.addActionListener(e -> entrar());
        add(btnEntrar, "growx, gaptop 10, h 40!");
        getRootPane().setDefaultButton(btnEntrar);

        add(Ui.tenue("Cuentas de ejemplo (contraseña 1234). Haz clic para llenar el correo:"), "gaptop 18");
        try {
            for (Usuario u : new UsuarioDao().todos()) {
                JButton b = new JButton(u.nombre() + " · " + u.rolesLegibles());
                b.setHorizontalAlignment(JButton.LEFT);
                b.addActionListener(e -> {
                    txtCorreo.setText(u.correo());
                    txtPass.requestFocusInWindow();
                });
                add(b, "growx");
            }
        } catch (SQLException e) {
            add(Ui.tenue("No se pudieron cargar las cuentas de ejemplo."));
        }
        add(Ui.tenue("<html>Las reglas son las que confirmó Gonzalo (Product Owner). Ver README.md.</html>"), "gaptop 10");

        pack();
        setLocationRelativeTo(null);
    }

    private void entrar() {
        try {
            Usuario u = auth.entrar(txtCorreo.getText(), new String(txtPass.getPassword()));
            dispose();
            new MainFrame(u).setVisible(true);
        } catch (ReglaException e) {
            lblError.setText(e.getMessage());
            txtPass.setText("");
        } catch (SQLException e) {
            lblError.setText("No se pudo consultar la base de datos.");
        }
    }
}
