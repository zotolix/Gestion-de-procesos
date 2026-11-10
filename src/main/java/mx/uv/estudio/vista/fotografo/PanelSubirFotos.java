package mx.uv.estudio.vista.fotografo;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mx.uv.estudio.modelo.Proyecto;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.FotoService;
import mx.uv.estudio.servicio.Formatos;
import mx.uv.estudio.util.Ui;
import mx.uv.estudio.vista.MainFrame;
import mx.uv.estudio.vista.Pantalla;
import mx.uv.estudio.vista.admin.Accion;
import mx.uv.estudio.vista.comun.SelectorArchivos;

/** HU03 · El fotógrafo sube las fotos de sus sesiones realizadas; el proyecto queda en Pendiente y el editor recibe aviso (HU06). */
public class PanelSubirFotos extends Pantalla {

    private final FotoService servicio = new FotoService();

    public PanelSubirFotos(Usuario usuario, MainFrame main) {
        super(usuario, main);
    }

    @Override
    protected void construir() throws SQLException {
        add(Ui.titulo("Subir fotos"));
        add(Ui.tenue("<html>Sube las fotos de una sesión ya realizada. El proyecto queda en Pendiente y el editor recibe un aviso "
                + "con el cliente y el número de fotos. Mientras siga Pendiente puedes agregar más.</html>"), "gapbottom 10");
        List<Sesion> pendientes = servicio.sesionesPorSubir(usuario);
        if (pendientes.isEmpty()) {
            add(Ui.tenue("No tienes sesiones realizadas por subir."));
        }
        for (Sesion s : pendientes) {
            add(tarjeta(s));
        }
        List<Proyecto> mios = servicio.proyectosDe(usuario);
        if (!mios.isEmpty()) {
            add(Ui.subtitulo("Mis proyectos"), "gaptop 14");
            for (Proyecto p : mios) {
                add(Ui.chip(p.cliente() + " · " + p.numFotos() + " fotos · " + p.estado(), color(p.estado())));
            }
        }
    }

    private JPanel tarjeta(Sesion s) throws SQLException {
        Optional<Proyecto> p = servicio.proyectoDe(s);
        String estado = p.isPresent() ? "Pendiente · " + p.get().numFotos() + " fotos ya subidas" : "Sin fotos todavía";
        JPanel t = Ui.tarjeta("fillx, gap 8", "[grow,fill][]");
        t.add(new JLabel("<html><b>" + Ui.escapar(s.clienteNombre()) + "</b><br><font color='" + Ui.HEX_TENUE + "'>"
                + Ui.fecha(s.fecha()) + " · " + s.paquete() + " · " + estado + "</font></html>"));
        JButton subir = Ui.primario(p.isPresent() ? "Agregar fotos" : "Elegir fotos y subir");
        subir.addActionListener(e -> {
            List<Path> elegidas = SelectorArchivos.elegir(this, "Fotos de " + s.clienteNombre(), Formatos.ORIGINALES);
            if (elegidas.isEmpty()) {
                return;
            }
            Accion.ejecutar(this, () -> servicio.subirFotos(s, elegidas, usuario), () -> {
                Ui.info(this, elegidas.size() + " fotos subidas. Se avisó al editor.");
                refrescar();
            });
        });
        t.add(subir);
        return t;
    }

    private static java.awt.Color color(String estado) {
        return switch (estado) {
            case "Terminado" -> Ui.OK;
            case "Pendiente" -> Ui.TENUE;
            default -> Ui.AVISO;
        };
    }
}
