package mx.uv.estudio.vista.comun;

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Diálogo para elegir varias fotos; devuelve lista vacía si la persona cancela. */
public final class SelectorArchivos {

    private SelectorArchivos() { }

    public static List<Path> elegir(Component padre, String titulo, List<String> extensiones) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(titulo);
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileNameExtensionFilter("Fotos (" + String.join(", ", extensiones) + ")",
                extensiones.toArray(new String[0])));
        if (fc.showOpenDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return List.of();
        }
        return Arrays.stream(fc.getSelectedFiles()).map(File::toPath).toList();
    }
}
