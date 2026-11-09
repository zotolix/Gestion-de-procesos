package mx.uv.estudio.vista.admin;

import java.awt.Component;
import java.sql.SQLException;
import javax.swing.JButton;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.util.Ui;

/** Botón que ejecuta una acción del servicio y muestra el error si la regla no se cumple. */
public final class Accion {

    public interface Tarea {
        void hacer() throws ReglaException, SQLException;
    }

    private Accion() { }

    public static JButton boton(Component padre, String texto, boolean primario, Tarea t, Runnable despues) {
        JButton b = primario ? Ui.primario(texto) : new JButton(texto);
        b.addActionListener(e -> ejecutar(padre, t, despues));
        return b;
    }

    /** Ejecuta la tarea y, si todo sale bien, corre «despues»; si no, muestra el error. */
    public static void ejecutar(Component padre, Tarea t, Runnable despues) {
        try {
            t.hacer();
            despues.run();
        } catch (ReglaException ex) {
            Ui.error(padre, Ui.lineas(ex.mensajes()));
        } catch (SQLException ex) {
            Ui.error(padre, "No se pudo guardar: " + ex.getMessage());
        }
    }
}
