package mx.uv.estudio;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mx.uv.estudio.util.Db;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.LoginFrame;

/** Punto de entrada. Corre este archivo (botón Run en VS Code) o "mvn compile exec:java". */
public class App {

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.focusColor", Ui.ACENTO);
        UIManager.put("Table.rowHeight", 30);
        try {
            Db.iniciar();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
