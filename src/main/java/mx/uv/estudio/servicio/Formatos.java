package mx.uv.estudio.servicio;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Formatos de archivo aceptados en cada etapa. */
public final class Formatos {

    /** Fotos que sube el fotógrafo (HU03): las que salen de la cámara. */
    public static final List<String> ORIGINALES = List.of("jpg", "jpeg", "png", "tif", "tiff",
            "raw", "cr2", "cr3", "nef", "arw", "dng", "raf", "orf", "rw2");
    /** Alta calidad (HU09): RAW, TIFF o JPG en máxima resolución, para impresión. */
    public static final List<String> ALTA = List.of("raw", "cr2", "cr3", "nef", "arw", "dng", "raf", "orf", "rw2",
            "tif", "tiff", "jpg", "jpeg");
    /** Baja calidad (HU09): JPG comprimido, para redes sociales. */
    public static final List<String> BAJA = List.of("jpg", "jpeg");

    private Formatos() { }

    public static String extension(Path p) {
        String n = p.getFileName().toString();
        int i = n.lastIndexOf('.');
        return i < 0 ? "" : n.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    /** Nombres de los archivos cuyo formato no está en la lista. */
    public static List<String> invalidos(List<Path> archivos, List<String> permitidos) {
        return archivos.stream().filter(a -> !permitidos.contains(extension(a)))
                .map(a -> a.getFileName().toString()).toList();
    }
}
