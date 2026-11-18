package mx.uv.estudio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.JTextField;
import mx.uv.estudio.modelo.Paquete;
import mx.uv.estudio.modelo.Pago;
import mx.uv.estudio.modelo.ReglaException;
import mx.uv.estudio.modelo.Sesion;
import mx.uv.estudio.modelo.Usuario;
import mx.uv.estudio.servicio.AgendaService;
import mx.uv.estudio.servicio.CajaService;
import mx.uv.estudio.servicio.InventarioService;
import mx.uv.estudio.servicio.UsuarioService;
import mx.uv.estudio.util.Db;
import mx.uv.estudio.vista.comun.Campos;

public class PruebaValidaciones {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBAS DE VALIDACIÓN Y FILTROS ===");
        try {
            Db.iniciar();
        } catch (Exception ignored) { }
        probarFiltroSoloLetras();
        probarFiltroSoloTelefono();
        probarFiltroSoloDecimal();
        probarFiltroSoloSerie();
        probarValidacionUsuarioService();
        probarValidacionAgendaService();
        probarValidacionInventarioService();
        probarBuscadorPorNombreEId();
        probarHistorialPagosPendientesYFiniquitados();
        probarBloqueoGuardadoDuplicadoFiniquitado();
        System.out.println("=== TODAS LAS PRUEBAS PASARON EXITOSAMENTE ===");
    }

    private static void afirmar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError("FALLO: " + mensaje);
        }
        System.out.println("  ✓ " + mensaje);
    }

    private static void probarFiltroSoloLetras() {
        System.out.println("\n1. Probando filtro Campos.soloLetras en JTextField:");
        JTextField tf = new JTextField();
        Campos.soloLetras(tf);

        tf.setText("José María Peña O'Connor-Smith");
        afirmar(tf.getText().equals("José María Peña O'Connor-Smith"), "Permite letras, acentos, ñ, apóstrofe y guion");

        tf.setText("Carlos123");
        afirmar(!tf.getText().equals("Carlos123"), "Bloquea números en campo de nombre");

        tf.setText("Ana@Estudio!");
        afirmar(!tf.getText().equals("Ana@Estudio!"), "Bloquea símbolos especiales en campo de nombre");
    }

    private static void probarFiltroSoloTelefono() {
        System.out.println("\n2. Probando filtro Campos.soloTelefono en JTextField:");
        JTextField tf = new JTextField();
        Campos.soloTelefono(tf);

        tf.setText("+52 (228) 123-4567");
        afirmar(tf.getText().equals("+52 (228) 123-4567"), "Permite dígitos y formato telefónico");

        tf.setText("228abc456");
        afirmar(!tf.getText().equals("228abc456"), "Bloquea letras en campo de teléfono");
    }

    private static void probarFiltroSoloDecimal() {
        System.out.println("\n3. Probando filtro Campos.soloDecimal en JTextField:");
        JTextField tf = new JTextField();
        Campos.soloDecimal(tf);

        tf.setText("1500.50");
        afirmar(tf.getText().equals("1500.50"), "Permite números con hasta dos decimales");

        tf.setText("abc");
        afirmar(!tf.getText().equals("abc"), "Bloquea letras en campo de monto");

        tf.setText("1500.555");
        afirmar(!tf.getText().equals("1500.555"), "Bloquea más de dos decimales");

        tf.setText("10.20.30");
        afirmar(!tf.getText().equals("10.20.30"), "Bloquea múltiples puntos decimales");
    }

    private static void probarFiltroSoloSerie() {
        System.out.println("\n4. Probando filtro Campos.soloSerie en JTextField:");
        JTextField tf = new JTextField();
        Campos.soloSerie(tf);

        tf.setText("CN-R6-0012");
        afirmar(tf.getText().equals("CN-R6-0012"), "Permite alfanumérico y guiones");

        tf.setText("CN R6@#$");
        afirmar(!tf.getText().equals("CN R6@#$"), "Bloquea caracteres no permitidos en serie");
    }

    private static void probarValidacionUsuarioService() {
        System.out.println("\n5. Probando validaciones en UsuarioService:");
        UsuarioService us = new UsuarioService();
        Usuario admin = new Usuario(1, "Elena", "elena@estudio.mx", List.of("administrador"));

        // Nombre con números
        try {
            us.crear("Juan123", "juan@estudio.mx", "1234", List.of("cajero"), admin);
            throw new AssertionError("Debió fallar con nombre con números");
        } catch (ReglaException e) {
            afirmar(e.mensajes().contains("El nombre solo debe contener letras."), "Rechaza nombre de usuario con números");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Nombre solo guiones o símbolos
        try {
            us.crear("---", "juan@estudio.mx", "1234", List.of("cajero"), admin);
            throw new AssertionError("Debió fallar con nombre sin letras");
        } catch (ReglaException e) {
            afirmar(e.mensajes().contains("El nombre solo debe contener letras."), "Rechaza nombre de usuario sin letras reales");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void probarValidacionAgendaService() {
        System.out.println("\n6. Probando validaciones en AgendaService:");
        AgendaService as = new AgendaService();
        Usuario cajero = new Usuario(1, "Ana", "ana@estudio.mx", List.of("cajero"));
        Usuario fotografo = new Usuario(2, "Bruno", "bruno@estudio.mx", List.of("fotografo"));
        Paquete paquete = new Paquete(1, "Básico", "1 h", new BigDecimal("800"), 60);

        // Nombre con números o caracteres inválidos
        AgendaService.Solicitud sInvalida = new AgendaService.Solicitud(
                "Cliente 123", "2281234567", "cliente@correo.com",
                LocalDate.now().plusDays(2), LocalTime.of(10, 0),
                fotografo, paquete, new BigDecimal("400"), "Efectivo"
        );
        try {
            as.agendar(sInvalida, cajero);
            throw new AssertionError("Debió fallar con nombre inválido");
        } catch (ReglaException e) {
            afirmar(e.mensajes().contains("El nombre del cliente solo debe contener letras."), "Rechaza nombre de cliente con números");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Teléfono con letras
        AgendaService.Solicitud sTelInvalido = new AgendaService.Solicitud(
                "Carlos Gómez", "telefono123", "cliente@correo.com",
                LocalDate.now().plusDays(2), LocalTime.of(10, 0),
                fotografo, paquete, new BigDecimal("400"), "Efectivo"
        );
        try {
            as.agendar(sTelInvalido, cajero);
            throw new AssertionError("Debió fallar con teléfono inválido");
        } catch (ReglaException e) {
            afirmar(e.mensajes().contains("El teléfono solo puede contener números, espacios, guiones o paréntesis."), "Rechaza teléfono con letras");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void probarValidacionInventarioService() {
        System.out.println("\n7. Probando validaciones en InventarioService:");
        InventarioService is = new InventarioService();
        Usuario admin = new Usuario(1, "Elena", "elena@estudio.mx", List.of("administrador"));

        try {
            is.agregar("Cámara Canon", "SERIE @# 123", "Cámara", admin);
            throw new AssertionError("Debió fallar con serie inválida");
        } catch (ReglaException e) {
            afirmar(e.mensajes().contains("El número de serie solo debe contener letras, números y guiones."), "Rechaza serie con caracteres inválidos");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void probarBuscadorPorNombreEId() {
        System.out.println("\n8. Probando HU (Buscador) - Búsqueda por Nombre y por ID en tiempo real:");
        AgendaService as = new AgendaService();
        try {
            // Búsqueda por nombre
            List<Sesion> porNombre = as.buscarPorCliente("Laura");
            afirmar(!porNombre.isEmpty() && porNombre.stream().anyMatch(s -> s.clienteNombre().contains("Laura")),
                    "Encuentra cliente por nombre ('Laura')");

            // Búsqueda por ID directo
            List<Sesion> porId = as.buscarPorCliente("2");
            afirmar(!porId.isEmpty() && porId.stream().anyMatch(s -> s.id() == 2),
                    "Encuentra sesión por ID numérico ('2')");

            // Búsqueda por ID con formato folio (#2)
            List<Sesion> porFolio = as.buscarPorCliente("#2");
            afirmar(!porFolio.isEmpty() && porFolio.stream().anyMatch(s -> s.id() == 2),
                    "Encuentra sesión por formato '#2'");

            // Búsqueda insensible a mayúsculas y acentos
            List<Sesion> porAcento = as.buscarPorCliente("mendez");
            afirmar(!porAcento.isEmpty() && porAcento.stream().anyMatch(s -> s.clienteNombre().contains("Méndez")),
                    "Encuentra cliente ignorando acentos y mayúsculas ('mendez' -> 'Méndez')");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void probarHistorialPagosPendientesYFiniquitados() {
        System.out.println("\n9. Probando HU (Historial) - Filtrado de 'Pagos Pendientes' y 'Pagos Finiquitados':");
        CajaService cs = new CajaService();
        try {
            List<Sesion> todas = cs.obtenerSesiones("TODOS", "");
            afirmar(!todas.isEmpty(), "Obtiene el registro maestro completo de clientes y sesiones");

            List<Sesion> pendientes = cs.obtenerSesiones("PENDIENTES", "");
            afirmar(!pendientes.isEmpty() && pendientes.stream().allMatch(s -> !s.liquidada()),
                    "Filtro 'Pagos Pendientes' solo incluye sesiones con saldo mayor a 0");

            List<Sesion> finiquitados = cs.obtenerSesiones("FINIQUITADOS", "");
            afirmar(!finiquitados.isEmpty() && finiquitados.stream().allMatch(Sesion::liquidada),
                    "Filtro 'Pagos Finiquitados' solo incluye sesiones completamente liquidadas");

            // Búsqueda combinada con filtro en tiempo real por ID
            List<Sesion> busquedaId = cs.obtenerSesiones("TODOS", "#1");
            afirmar(!busquedaId.isEmpty() && busquedaId.get(0).id() == 1,
                    "Filtrado en tiempo real en historial por ID (#1)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void probarBloqueoGuardadoDuplicadoFiniquitado() {
        System.out.println("\n10. Probando Bug (Validación) - Bloqueo de cobro duplicado a cliente finiquitado:");
        CajaService cs = new CajaService();
        Usuario cajero = new Usuario(1, "Ana", "ana@estudio.mx", List.of("cajero"));

        try {
            // Sesion 6 o 7 ya están finiquitadas en los datos de prueba
            List<Sesion> finiquitados = cs.obtenerSesiones("FINIQUITADOS", "");
            afirmar(!finiquitados.isEmpty(), "Existen sesiones finiquitadas para la prueba");
            Sesion sesFiniquitada = finiquitados.get(0);

            try {
                cs.liquidar(sesFiniquitada.id(), new BigDecimal("100"), "Efectivo", cajero);
                throw new AssertionError("Debió bloquear el intento de volver a guardar como finiquitado");
            } catch (ReglaException e) {
                afirmar(e.mensajes().stream().anyMatch(m -> m.contains("ya tiene su pago finiquitado")),
                        "Detecta cliente finiquitado y bloquea el guardado duplicado con alerta");
            }

            // Confirmar que el cliente maestro sigue existiendo en el historial tras el intento
            List<Sesion> postIntento = cs.obtenerSesiones("TODOS", String.valueOf(sesFiniquitada.id()));
            afirmar(!postIntento.isEmpty() && postIntento.get(0).liquidada(),
                    "Nunca elimina el registro maestro del cliente del buscador (conserva su estado finiquitado)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
