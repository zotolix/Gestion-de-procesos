package mx.uv.estudio.modelo;

import java.util.List;

/** Persona que entra al sistema. Puede tener varios roles: cajero, fotografo, editor, administrador. */
public record Usuario(int id, String nombre, String correo, List<String> roles) {

    public static final List<String> ROLES = List.of("cajero", "fotografo", "editor", "administrador");

    public boolean tiene(String rol) {
        return roles.contains(rol);
    }

    public static String legible(String rol) {
        return switch (rol) {
            case "cajero" -> "Cajero";
            case "fotografo" -> "Fotógrafo";
            case "editor" -> "Editor";
            case "administrador" -> "Administrador";
            default -> rol;
        };
    }

    public String rolesLegibles() {
        return String.join(", ", roles.stream().map(Usuario::legible).toList());
    }

    @Override
    public String toString() {
        return nombre;
    }
}
