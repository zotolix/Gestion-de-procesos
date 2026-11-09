package mx.uv.estudio.modelo;

import java.time.LocalDate;

/** Aviso dentro del sistema para un rol (editor o cajero). */
public record Notificacion(int id, String rol, String texto, LocalDate fecha, boolean leida) { }
