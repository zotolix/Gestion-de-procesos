package mx.uv.estudio.vista;

import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.util.Ui;

/** Pantalla de un rol que todavía no tiene módulos en esta versión del sistema (por ejemplo, el Editor). */
public class PanelAviso extends Pantalla {

    private final String titulo;
    private final String texto;

    public PanelAviso(Usuario usuario, MainFrame main, String titulo, String texto) {
        super(usuario, main);
        this.titulo = titulo;
        this.texto = texto;
    }

    @Override
    protected void construir() {
        add(Ui.titulo(titulo));
        add(Ui.tenue("<html>" + Ui.escapar(texto) + "</html>"), "w 520!");
    }
}
