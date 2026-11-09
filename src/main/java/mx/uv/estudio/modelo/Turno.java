package mx.uv.estudio.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Turno de caja de un día: quién lo abrió y con qué fondo (HU12). */
public record Turno(int id, LocalDate fecha, String cajero, BigDecimal fondo, LocalTime apertura) { }
