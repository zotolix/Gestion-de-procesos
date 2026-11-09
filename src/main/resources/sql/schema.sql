-- Tablas del sistema. Funciona igual en H2 (modo SQL Server) y en SQL Server.

CREATE TABLE usuario (
  id INT IDENTITY(1,1) PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  correo VARCHAR(120) NOT NULL UNIQUE,
  hash VARCHAR(100) NOT NULL           -- contraseña con BCrypt
);

CREATE TABLE usuario_rol (
  usuario_id INT NOT NULL REFERENCES usuario(id),
  rol VARCHAR(20) NOT NULL,            -- cajero, fotografo, editor, administrador
  PRIMARY KEY (usuario_id, rol)
);

CREATE TABLE paquete (
  id INT IDENTITY(1,1) PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  incluye VARCHAR(300) NOT NULL,
  precio DECIMAL(10,2) NOT NULL,       -- precio final, IVA incluido
  duracion_min INT,                    -- vacío = sin límite (bloquea el día)
  activo BIT DEFAULT 1 NOT NULL        -- 0 = paquete retirado del catálogo (ya tiene sesiones)
);

CREATE TABLE sesion (
  id INT IDENTITY(1,1) PRIMARY KEY,
  fecha DATE NOT NULL,
  hora TIME NOT NULL,
  fotografo_id INT NOT NULL REFERENCES usuario(id),
  paquete_id INT NOT NULL REFERENCES paquete(id),
  precio DECIMAL(10,2) NOT NULL,       -- precio del paquete al agendar
  duracion_min INT,                    -- duración del paquete al agendar
  cliente_nombre VARCHAR(120) NOT NULL,
  cliente_telefono VARCHAR(30) NOT NULL,
  cliente_correo VARCHAR(120) NOT NULL,
  anotaciones VARCHAR(1000) DEFAULT '' NOT NULL,
  estado VARCHAR(20) DEFAULT 'Agendada' NOT NULL   -- Agendada (Cancelada llega con HU23)
);

CREATE TABLE pago (
  id INT IDENTITY(1,1) PRIMARY KEY,
  sesion_id INT NOT NULL REFERENCES sesion(id),
  concepto VARCHAR(20) NOT NULL,       -- Anticipo (menos del total) o Liquidación (el total)
  monto DECIMAL(10,2) NOT NULL,
  metodo VARCHAR(20) NOT NULL,         -- Efectivo, Tarjeta, Transferencia
  fecha DATE NOT NULL
);

CREATE TABLE turno (
  id INT IDENTITY(1,1) PRIMARY KEY,
  fecha DATE NOT NULL UNIQUE,          -- un turno por día
  usuario_id INT NOT NULL REFERENCES usuario(id),
  fondo DECIMAL(10,2) NOT NULL,
  hora_apertura TIME NOT NULL,
  hora_cierre TIME,
  esperado DECIMAL(10,2),
  contado DECIMAL(10,2),
  revisado BIT DEFAULT 0 NOT NULL,
  revisado_por VARCHAR(100)
);

CREATE TABLE equipo (
  id INT IDENTITY(1,1) PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  numero_serie VARCHAR(60) NOT NULL,
  categoria VARCHAR(20) NOT NULL,      -- Cámara, Lente o Accesorio
  estado VARCHAR(20) NOT NULL,         -- Disponible, Dañado o En mantenimiento
  reporte_texto VARCHAR(500),
  reporte_por VARCHAR(100),
  reporte_fecha DATE
);

CREATE TABLE sesion_equipo (
  sesion_id INT NOT NULL REFERENCES sesion(id),
  equipo_id INT NOT NULL REFERENCES equipo(id),
  PRIMARY KEY (sesion_id, equipo_id)
);

CREATE TABLE notificacion (
  id INT IDENTITY(1,1) PRIMARY KEY,
  rol VARCHAR(20) NOT NULL,
  texto VARCHAR(300) NOT NULL,
  fecha DATE NOT NULL,
  leida BIT DEFAULT 0 NOT NULL
);

CREATE TABLE proyecto (
  id INT IDENTITY(1,1) PRIMARY KEY,
  sesion_id INT NOT NULL REFERENCES sesion(id),
  num_fotos INT NOT NULL,
  estado VARCHAR(20) NOT NULL,         -- Pendiente, En proceso, Revisión o Terminado
  aprobado BIT DEFAULT 0 NOT NULL,     -- 1 = el administrador aprobó la revisión (HU20)
  comentario VARCHAR(500)              -- comentario del administrador al regresar a En proceso
);

CREATE TABLE foto (
  id INT IDENTITY(1,1) PRIMARY KEY,
  proyecto_id INT NOT NULL REFERENCES proyecto(id),
  nombre VARCHAR(255) NOT NULL,
  calidad VARCHAR(10) NOT NULL,        -- Original (la sube el fotógrafo), Alta o Baja (las sube el editor)
  ruta VARCHAR(500) NOT NULL           -- dónde quedó el archivo dentro de la carpeta fotos
);
