package mx.uv.estudio.vista.editor;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mx.uv.estudio.modelo.Proyecto;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.EdicionService;
import mx.uv.estudio.servicio.Formatos;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import mx.uv.estudio.vista.admin.Accion;
import mx.uv.estudio.vista.comun.SelectorArchivos;

/**
 * HU07 · El editor etiqueta el avance de cada proyecto (Pendiente, En proceso, Revisión).
 * HU09 · Con el proyecto aprobado, sube las versiones en alta y baja calidad.
 */
public class PanelProyectosEditor extends Pantalla {

    private final EdicionService servicio = new EdicionService();

    public PanelProyectosEditor(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Proyectos"));
        add(Ui.tenue("<html>Marca el avance de cada proyecto. Cuando lo mandes a Revisión, el administrador lo aprueba o lo regresa "
                + "con un comentario; ya aprobado, sube las dos calidades.</html>"), "gapbottom 10");
        List<Proyecto> todos = servicio.todos();
        if (todos.isEmpty()) {
            add(Ui.tenue("Todavía no hay proyectos. Aparecen cuando un fotógrafo sube las fotos de una sesión."));
        }
        for (Proyecto p : todos) {
            add(tarjeta(p));
        }
    }

    private JPanel tarjeta(Proyecto p) throws SQLException {
        JPanel t = Ui.tarjeta("fillx, wrap 3, gap 8", "[grow,fill][][]");
        t.add(new JLabel("<html><b>" + Ui.escapar(p.cliente()) + "</b><br><font color='" + Ui.HEX_TENUE + "'>Sesión del "
                + Ui.fecha(p.fechaSesion()) + " · " + p.numFotos() + " fotos</font></html>"));
        t.add(Ui.chip(p.estado() + (p.aprobado() ? " · aprobado" : ""), color(p)));

        String siguiente = EdicionService.siguiente(p);
        if (siguiente != null) {
            t.add(Accion.boton(this, siguiente.equals("Revisión") ? "Enviar a Revisión" : "Pasar a " + siguiente, true,
                    () -> servicio.avanzar(p, usuario), this::refrescar));
        } else {
            t.add(new JLabel(), "w 10!");
        }
        if (p.comentario() != null && !p.comentario().isBlank()) {
            t.add(new JLabel("<html><font color='" + Ui.HEX_ACENTO + "'>Comentario del administrador: " + Ui.escapar(p.comentario())
                    + "</font></html>"), "span 3");
        }
        if ("Revisión".equals(p.estado()) && !p.aprobado()) {
            t.add(Ui.tenue("Esperando la revisión del administrador."), "span 3");
        }
        if (p.aprobado()) {
            int alta = servicio.cuantas(p, "Alta");
            int baja = servicio.cuantas(p, "Baja");
            t.add(new JLabel("<html>Alta calidad (RAW, TIFF o JPG, para impresión): <b>" + alta
                    + "</b> · Baja calidad (JPG comprimido, para redes): <b>" + baja + "</b></html>"), "span 3");
            t.add(subir(p, "Alta", Formatos.ALTA, "Subir alta calidad"), "span 3, split 2");
            t.add(subir(p, "Baja", Formatos.BAJA, "Subir baja calidad"));
            if (alta > 0 && baja > 0) {
                t.add(Ui.tenue("Con las dos calidades subidas, el proyecto queda listo para marcarse Terminado (HU10, Sprint 4)."),
                        "span 3");
            }
        }
        return t;
    }

    private javax.swing.JButton subir(Proyecto p, String calidad, List<String> formatos, String texto) {
        javax.swing.JButton b = new javax.swing.JButton(texto);
        b.addActionListener(e -> {
            List<Path> elegidas = SelectorArchivos.elegir(this, texto + " · " + p.cliente(), formatos);
            if (elegidas.isEmpty()) {
                return;
            }
            Accion.ejecutar(this, () -> servicio.subirCalidad(p, calidad, elegidas, usuario), () -> {
                Ui.info(this, elegidas.size() + " archivos subidos en " + calidad.toLowerCase() + " calidad.");
                refrescar();
            });
        });
        return b;
    }

    private static java.awt.Color color(Proyecto p) {
        return switch (p.estado()) {
            case "Terminado" -> Ui.OK;
            case "Pendiente" -> Ui.TENUE;
            case "Revisión" -> p.aprobado() ? Ui.OK : Ui.AVISO;
            default -> Ui.AVISO;
        };
    }
}
