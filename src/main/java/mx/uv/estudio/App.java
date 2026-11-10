package mx.uv.estudio;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.inter.FlatInterFont;
import java.awt.Font;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mx.uv.estudio.util.Db;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.LoginFrame;

/** Punto de entrada. Corre este archivo (botón Run en VS Code) o "mvn compile exec:java". */
public class App {

    public static void main(String[] args) {
        // 1. Instalar y configurar tipografía Inter
        FlatInterFont.install();
        FlatLaf.setPreferredFontFamily(FlatInterFont.FAMILY);
        FlatLaf.setPreferredLightFontFamily(FlatInterFont.FAMILY_LIGHT);
        FlatLaf.setPreferredSemiboldFontFamily(FlatInterFont.FAMILY_SEMIBOLD);

        // 2. Establecer apariencia oscura moderna
        FlatDarkLaf.setup();

        // 3. Agrandar la tipografía global un 25% con fuente Inter
        Font baseFont = UIManager.getFont("defaultFont");
        int scaledSize = (baseFont != null) ? Math.round(baseFont.getSize2D() * 1.25f) : 16;
        Font interFont = new Font(FlatInterFont.FAMILY, Font.PLAIN, scaledSize);
        UIManager.put("defaultFont", interFont);

        // 4. Suavizado de bordes en todos los componentes y ventanas
        UIManager.put("Button.arc", 16);
        UIManager.put("Component.arc", 16);
        UIManager.put("TextComponent.arc", 16);
        UIManager.put("CheckBox.arc", 8);
        UIManager.put("ProgressBar.arc", 16);
        UIManager.put("ScrollPane.arc", 16);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("Spinner.arc", 16);
        UIManager.put("ComboBox.arc", 16);
        UIManager.put("PopupMenu.arc", 16);
        UIManager.put("TitlePane.arc", 16);

        // 5. Paleta de colores: negro 1A1A1A, blanco y detalles en amarillo F7FF5E
        UIManager.put("Component.focusColor", Ui.ACENTO);
        UIManager.put("Component.focusedBorderColor", Ui.ACENTO);
        UIManager.put("Component.borderColor", Ui.BORDE);

        UIManager.put("Panel.background", Ui.FONDO);
        UIManager.put("control", Ui.FONDO);
        UIManager.put("window", Ui.FONDO);

        UIManager.put("Button.default.background", Ui.ACENTO);
        UIManager.put("Button.default.foreground", Ui.OSCURO);

        UIManager.put("ToggleButton.selectedBackground", Ui.ACENTO);
        UIManager.put("ToggleButton.selectedForeground", Ui.OSCURO);

        UIManager.put("Table.rowHeight", 36);
        UIManager.put("Table.selectionBackground", Ui.ACENTO);
        UIManager.put("Table.selectionForeground", Ui.OSCURO);

        UIManager.put("List.selectionBackground", Ui.ACENTO);
        UIManager.put("List.selectionForeground", Ui.OSCURO);

        UIManager.put("TextField.caretColor", Ui.ACENTO);
        UIManager.put("PasswordField.caretColor", Ui.ACENTO);
        UIManager.put("TextArea.caretColor", Ui.ACENTO);

        try {
            Db.iniciar();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
