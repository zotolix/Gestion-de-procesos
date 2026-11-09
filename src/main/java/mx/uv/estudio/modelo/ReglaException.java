package mx.uv.estudio.modelo;

import java.util.List;

/** Se lanza cuando los datos no cumplen una regla del negocio. Lleva los mensajes para el usuario. */
public class ReglaException extends Exception {

    private final List<String> mensajes;

    public ReglaException(List<String> mensajes) {
        super(String.join(" ", mensajes));
        this.mensajes = List.copyOf(mensajes);
    }

    public ReglaException(String mensaje) {
        this(List.of(mensaje));
    }

    public List<String> mensajes() {
        return mensajes;
    }
}
