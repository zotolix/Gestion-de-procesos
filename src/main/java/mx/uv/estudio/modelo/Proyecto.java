package mx.uv.estudio.modelo;

import java.time.LocalDate;

/** Proyecto de edición de una sesión (estados: Pendiente, En proceso, Revisión, Terminado). */
public record Proyecto(int id, int sesionId, String cliente, LocalDate fechaSesion, int numFotos,
                       String estado, boolean aprobado, String comentario) { }
