package mx.uv.estudio.vista;

import java.sql.SQLException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Ui;
import net.miginfocom.swing.MigLayout;

/**
 * Base de todas las pantallas. Cada pantalla implementa construir(), que arma su contenido
 * con los datos actuales. refrescar() la vuelve a armar (al entrar o después de guardar).
 */
public abstract class Pantalla extends JPanel {

    protected final Usuario usuario;
    protected final MainFrame main;

    protected Pantalla(Usuario usuario, MainFrame main) {
        this.usuario = usuario;
        this.main = main;
        setLayout(new MigLayout("insets 18 22, fillx, wrap 1", "[grow,fill]"));
    }

    protected abstract void construir() throws SQLException;

    public void refrescar() {
        removeAll();
        try {
            construir();
        } catch (SQLException e) {
            JLabel l = new JLabel("No se pudieron cargar los datos: " + e.getMessage());
            l.setForeground(Ui.MAL);
            add(l);
        }
        revalidate();
        repaint();
        main.actualizarMenu();
    }
}
