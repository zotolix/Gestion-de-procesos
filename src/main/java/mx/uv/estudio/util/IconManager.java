package mx.uv.estudio.util;

import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * Carga y escala los iconos del sistema de forma segura.
 * Si un archivo no existe, devuelve null en lugar de lanzar NullPointerException.
 */
public final class IconManager {

    private IconManager() { }

    /** Carga un icono desde /icons/ y lo escala a w x h. Devuelve null si no se encuentra. */
    public static ImageIcon cargar(String nombre, int w, int h) {
        try {
            java.net.URL url = IconManager.class.getResource("/icons/" + nombre);
            if (url == null) {
                return null;
            }
            Image img = new ImageIcon(url).getImage();
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    /** Versión sin escalar (tamaño original del PNG). */
    public static ImageIcon cargar(String nombre) {
        try {
            java.net.URL url = IconManager.class.getResource("/icons/" + nombre);
            if (url == null) {
                return null;
            }
            return new ImageIcon(url);
        } catch (Exception e) {
            return null;
        }
    }
}
