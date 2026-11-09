package mx.uv.estudio.servicio;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mx.uv.estudio.dao.EquipoDao;
import mx.uv.estudio.dao.SesionDao;
import mx.uv.estudio.dao.UsuarioDao;
import mx.uv.estudio.modelo.Equipo;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Usuario;

/**
 * Revisa cruces de horario (HU01, HU02).
 * Una sesión ocupa de su hora de inicio a inicio + duración del paquete.
 * Si el paquete no tiene límite (Premium), ocupa el día completo.
 */
public class Disponibilidad {

    private final SesionDao sesiones = new SesionDao();
    private final UsuarioDao usuarios = new UsuarioDao();
    private final EquipoDao equipo = new EquipoDao();

    public static LocalDateTime inicio(LocalDate f, LocalTime h, Integer dur) {
        return dur == null ? f.atStartOfDay() : f.atTime(h);
    }

    public static LocalDateTime fin(LocalDate f, LocalTime h, Integer dur) {
        return dur == null ? f.plusDays(1).atStartOfDay() : f.atTime(h).plusMinutes(dur);
    }

    private static boolean cruza(Sesion s, LocalDate f, LocalTime h, Integer dur) {
        return s.inicio().isBefore(fin(f, h, dur)) && inicio(f, h, dur).isBefore(s.fin());
    }

    /** Sesión con la que se cruzaría el fotógrafo, si hay. excluir: id de la sesión que se está moviendo (o 0). */
    public Optional<Sesion> choqueFotografo(int fotografoId, LocalDate f, LocalTime h, Integer dur, int excluir) throws SQLException {
        for (Sesion s : sesiones.agendadasDeFotografo(fotografoId, f)) {
            if (s.id() != excluir && cruza(s, f, h, dur)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public Optional<Sesion> choqueEquipo(int equipoId, LocalDate f, LocalTime h, Integer dur, int excluir) throws SQLException {
        for (Sesion s : sesiones.agendadasConEquipo(equipoId, f)) {
            if (s.id() != excluir && cruza(s, f, h, dur)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public List<Usuario> fotografosLibres(LocalDate f, LocalTime h, Integer dur, int excluir) throws SQLException {
        List<Usuario> r = new ArrayList<>();
        for (Usuario u : usuarios.conRol("fotografo")) {
            if (choqueFotografo(u.id(), f, h, dur, excluir).isEmpty()) {
                r.add(u);
            }
        }
        return r;
    }

    /** Equipo disponible (no dañado ni en mantenimiento) y sin otra sesión en ese horario. */
    public List<Equipo> equipoLibre(LocalDate f, LocalTime h, Integer dur, int excluir) throws SQLException {
        List<Equipo> r = new ArrayList<>();
        for (Equipo e : equipo.todos()) {
            if (e.disponible() && choqueEquipo(e.id(), f, h, dur, excluir).isEmpty()) {
                r.add(e);
            }
        }
        return r;
    }

    /** ¿El equipo está en una sesión que ocurre en este momento? Solo para mostrarlo en el inventario. */
    public boolean enUsoAhora(Equipo e) throws SQLException {
        LocalDateTime ahora = LocalDateTime.now();
        for (Sesion s : sesiones.agendadasConEquipo(e.id(), ahora.toLocalDate())) {
            if (!ahora.isBefore(s.inicio()) && ahora.isBefore(s.fin())) {
                return true;
            }
        }
        return false;
    }
}
