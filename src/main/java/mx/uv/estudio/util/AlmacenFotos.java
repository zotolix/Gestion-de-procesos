package mx.uv.estudio.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Guarda las fotos en la carpeta «fotos» junto a la base de datos: fotos/proyecto-ID/original|alta|baja.
 * Es un almacén local; si el equipo decide usar Google Drive (HU03 original), solo cambia esta clase.
 */
public final class AlmacenFotos {

    private static final Path RAIZ = Path.of("fotos");

    private AlmacenFotos() { }

    /** Copia el archivo a la carpeta del proyecto y devuelve dónde quedó. */
    public static Path copiar(int proyectoId, String carpeta, Path origen) throws IOException {
        Path dir = RAIZ.resolve("proyecto-" + proyectoId).resolve(carpeta);
        Files.createDirectories(dir);
        String nombre = origen.getFileName().toString();
        Path destino = dir.resolve(nombre);
        int n = 2;
        while (Files.exists(destino)) {   // no pisa un archivo con el mismo nombre
            int punto = nombre.lastIndexOf('.');
            String base = punto < 0 ? nombre : nombre.substring(0, punto);
            String ext = punto < 0 ? "" : nombre.substring(punto);
            destino = dir.resolve(base + " (" + n++ + ")" + ext);
        }
        return Files.copy(origen, destino);
    }

    public static void borrar(Path archivo) {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            // si no se puede borrar, queda un archivo suelto sin registro; no afecta al sistema
        }
    }
}
