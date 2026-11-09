# Sistema del estudio fotográfico · Sprint 1, 2 y 3 + HU17 a HU20

Aplicación de escritorio en Java (Swing). Esta versión incluye **solo las historias de usuario de los Sprints 1 y 2**
mientras Gonzalo (Product Owner) confirma las HU nuevas. Las reglas son las que confirmó Gonzalo.

| Sprint | HU incluidas |
| --- | --- |
| 1 · Acceso y agenda | HU16 Iniciar sesión por rol · HU01 Consultar calendario · HU15 Registrar anticipo · HU11 Catálogo de paquetes · HU04 Anotaciones del cliente |
| 2 · Caja y control de equipo | HU12 Registrar fondo de caja · HU02 Gestionar inventario · HU05 Reportar equipo dañado |
| 3 · Flujo de fotos al editor | HU03 Subir fotos · HU06 Notificar al editor · HU07 Etiquetar avance · HU09 Alta y baja calidad |
| Administrador (HU nuevas) | HU17 Crear usuarios y roles · HU18 Cambiar paquetes y precios · HU19 Atender equipo dañado · HU20 Revisar proyectos |

## Qué necesitas

1. **JDK 25 o más nuevo** (`java -version`).
2. **VS Code** con la extensión **Extension Pack for Java** (trae Maven).
3. **Internet la primera vez**: Maven descarga FlatLaf, MigLayout, H2 y jBCrypt.

No hace falta instalar base de datos: se usa **H2** (modo SQL Server) y guarda todo en la carpeta `datos/`.
Si tenías una versión anterior, la aplicación detecta la base vieja y la recrea sola; también sirve el botón *Restablecer datos de ejemplo*.

## Cómo correrlo

1. Descomprime el .zip y abre la carpeta `sistema-estudio` en VS Code.
2. Espera a que diga "Java: Ready".
3. Abre `src/main/java/mx/uv/estudio/App.java` y haz clic en **Run** encima de `main`. (O en terminal: `mvn compile exec:java`.)

## Cuentas de ejemplo (contraseña **1234**)

| Correo | Roles |
| --- | --- |
| ana@estudio.mx | Cajero |
| bruno@estudio.mx | Fotógrafo |
| carla@estudio.mx | Fotógrafo y Editor |
| diego@estudio.mx | Editor (todavía sin pantallas) |
| elena@estudio.mx | Administrador |

Se pueden abrir varias ventanas, una por cuenta.

## Quién hace qué en esta versión

| Rol | Pantallas |
| --- | --- |
| Cajero | Catálogo de paquetes · Agendar y anticipo (con buscador por nombre) · Fondo de caja |
| Fotógrafo | **Subir fotos (HU03)** · Mi calendario (ve sus sesiones, escribe anotaciones, asigna equipo) · Inventario (reporta daños) |
| Administrador | Avisos · **Revisión de proyectos (HU20)** · **Usuarios y roles (HU17)** · Paquetes y precios (HU18, CRUD) · Inventario (HU19: agrega equipo y atiende el dañado o en mantenimiento) |
| Editor | **Proyectos (HU07, HU09)** · Avisos (HU06: fotos nuevas; también ve si el administrador aprobó o regresó un proyecto) |

## Reglas que aplica el sistema

| Tema | Regla |
| --- | --- |
| Agendar | **Solo lo hace el cajero.** El fotógrafo no agenda ni reprograma. Solo aparecen los fotógrafos libres en ese horario. |
| Buscador | En *Agendar*, escribe el nombre del cliente (sin importar mayúsculas ni acentos) y salen sus sesiones; *Usar datos* llena nombre, teléfono y correo. |
| Pago al agendar | **Mínimo el 50 %** del paquete; se puede pagar más o el total (botones *Mínimo 50 %* y *Pagar el total*). Si paga el total, la sesión queda **Liquidada**. Efectivo, tarjeta o transferencia. |
| Paquetes | Básico $800 (1 h), Estándar $1,800 (2 h, álbum incluido), Premium $3,500 (sin límite: bloquea el día del fotógrafo). Precios con IVA incluido. |
| CRUD de paquetes | El administrador agrega, edita y elimina. Un paquete con sesiones ya agendadas no se borra: se retira del catálogo y esas sesiones lo conservan. |
| Calendario | Dos sesiones del mismo fotógrafo no pueden cruzarse. |
| Equipo | Nombre, número de serie (no se repite) y categoría (cámara, lente o accesorio). Estados: Disponible, Dañado, En mantenimiento. "En uso" solo durante el horario de la sesión. **Lo agrega el administrador**; el fotógrafo lo asigna a su sesión y reporta daños. Dañado o en mantenimiento no se puede asignar ni se cruza entre sesiones. |
| Daño | El fotógrafo lo describe, el equipo pasa a Dañado, se quita de las sesiones que vienen y el administrador recibe el aviso. |
| Fotos (HU03, HU06) | El fotógrafo sube las fotos de una sesión **ya iniciada** (JPG, PNG, TIFF o RAW). Se crea el proyecto en Pendiente y el editor recibe un aviso con el cliente y el número de fotos. Mientras siga Pendiente puede agregar más. Las fotos se copian a la carpeta `fotos/proyecto-ID/original` junto a la base de datos (es un almacén local, no Google Drive; solo habría que cambiar la clase `AlmacenFotos`). |
| Avance (HU07) | El editor mueve el proyecto completo: Pendiente → En proceso → Revisión. Al enviarlo a Revisión, el administrador recibe un aviso. De Revisión lo mueve el administrador (HU20); Terminado llega con HU10 (Sprint 4). |
| Calidades (HU09) | Con el proyecto aprobado por el administrador, el editor sube la versión **alta** (RAW, TIFF o JPG, para impresión) en `alta/` y la **baja** (solo JPG comprimido, para redes) en `baja/`. |
| Usuarios (HU17) | Solo el administrador crea cuentas: nombre, correo (no se repite), contraseña y uno o más roles. Puede cambiar los roles después; no se le puede quitar el rol Administrador al único administrador. |
| Revisión (HU20) | El administrador ve los proyectos en Revisión. **Aprobar** deja el proyecto listo para que el editor suba las dos calidades y lo marque Terminado; **Regresar** lo pasa a En proceso y exige un comentario que ve el editor. Los proyectos de ejemplo traen un comentario o estado ya cargado; los reales nacen al subir fotos (HU03). |
| Caja | Un turno al día y un solo cajero a la vez. Al abrirlo se registra el fondo. |

## Recorrido para la demo

1. **Cajero (Ana):** *Fondo de caja* → abre el turno. *Agendar y anticipo* → busca "mendez" → *Usar datos* → agenda pagando el 50 %, más del 50 % o el total.
2. **Fotógrafo (Bruno):** *Mi calendario* → abre la sesión, escribe anotaciones y asigna equipo. *Inventario* → reporta un daño.
3. **Administrador (Elena):** *Revisión de proyectos* → aprueba uno y regresa otro con comentario; entra como Diego (Editor) y mira *Avisos*. *Usuarios y roles* → crea un usuario con dos roles. *Avisos* → ve el aviso. *Inventario de equipo* → agrega una cámara o lente y pasa el equipo dañado a mantenimiento y a disponible. *Paquetes y precios* → crea, edita y elimina.

El botón **Restablecer datos de ejemplo** (abajo a la derecha) vuelve a cargar los datos iniciales.

## Organización del código

```
src/main/java/mx/uv/estudio/
  App.java                 punto de entrada
  modelo/                  datos (Usuario, Paquete, Sesion, Pago, Turno, Equipo, Notificacion, Proyecto)
  dao/                     consultas SQL con JDBC
  servicio/                reglas de cada HU (AuthService, AgendaService, Disponibilidad,
                           PaqueteService, InventarioService, CajaService, UsuarioService, RevisionService, FotoService, EdicionService)
  vista/                   pantallas Swing
    comun/                 PanelAgendar (HU15), PanelInventario (HU02, HU05)
    cajero/                PanelCatalogo (HU11), PanelTurno (HU12)
    fotografo/             PanelCalendario (HU01, HU04, asignar equipo)
    admin/                 PanelPaquetes (HU18), PanelAvisos, PanelUsuarios (HU17), PanelRevision (HU20)
    editor/                PanelAvisosEditor, PanelProyectosEditor (HU07, HU09)
    fotografo/             PanelCalendario, PanelSubirFotos (HU03)
    LoginFrame, MainFrame  inicio de sesión (HU16) y ventana principal
  util/                    Db (conexión), Semilla (datos de ejemplo), Ui (estilos)
src/main/resources/sql/schema.sql   tablas
```

Cada acción va así: la pantalla (`vista`) le pide al `servicio`, el servicio valida las reglas y llama al `dao`, y el dao ejecuta el SQL.

## Todavía no incluido (Sprints 3 y 4 y HU por aprobar)

- Marcar Terminado (HU10), estado de trabajos (HU13) y recibos por correo (HU14).
- HU por aprobar: cobrar la liquidación (HU21), corte de caja (HU22, con el comentario cuando salga negativo), cancelar o reprogramar (HU23).
- HU20 trae 3 proyectos de ejemplo; los reales nacen al subir fotos (HU03). La sesión de Sofía Paredes (fotógrafa Carla) queda sin proyecto para probar la subida.
- Para el Sprint 4 quedan HU10 (marcar Terminado), HU13 (estado de trabajos) y HU14 (recibos por correo), más HU21 a HU23.
- Pendiente con Gonzalo: si se devuelve el anticipo al cancelar.
- El fondo que se deja para el día siguiente es interno del estudio; no se programa.

## Cambiar a SQL Server

1. En SQL Server: `CREATE DATABASE estudio;`
2. En `pom.xml` agrega el driver `com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11`.
3. En `util/Db.java` cambia `URL`, `USUARIO` y `CLAVE` (`jdbc:sqlserver://localhost:1433;databaseName=estudio;encrypt=false`).
4. Corre la aplicación: al primer inicio crea las tablas de `schema.sql` y carga los datos de ejemplo.
