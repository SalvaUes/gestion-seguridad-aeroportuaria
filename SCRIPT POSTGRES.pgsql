DELETE FROM agente_habilidades;

DELETE FROM agentes;

SELECT * FROM agentes;


ALTER SEQUENCE agentes_id_agente_seq RESTART WITH 1;


-- ====================================================================
-- !! IMPORTANTE: EJECUTAR ESTE SCRIPT COMPLETO EN UNA ÚNICA SESIÓN !!
-- No ejecutar solo los INSERTs o causará errores de clave duplicada.
-- Este script está diseñado para RESETEAR el entorno de pruebas de personal.
-- ====================================================================

-- PASO 1: Limpiar completamente la tabla de agentes.
-- Esto es VITAL para eliminar los datos creados por la aplicación al iniciar.
-- Se eliminan también las dependencias en tablas relacionadas para evitar errores de clave foránea.
TRUNCATE TABLE agente_habilidades, permiso_agente_aerolinea, agentes RESTART IDENTITY CASCADE;

-- Explicación de la línea anterior:
-- TRUNCATE es más rápido que DELETE.
-- RESTART IDENTITY reinicia la secuencia de IDs (equivalente a ALTER SEQUENCE ... RESTART WITH 1).
-- CASCADE elimina registros en tablas relacionadas que dependen de 'agentes'.

-- === COORDINADORES (Nivel más alto, sin superior) ===
INSERT INTO agentes (nombre, apellido, numero_carnet, genero, direccion, fecha_nacimiento, telefono, email, ruta_fotografia, activo, rol, id_superior) VALUES
('Carlos', 'Rivera', 'C-001', 'MASCULINO', 'Av. Principal 123', '1980-05-15', '555-0101', 'carlos.rivera@example.com', NULL, true, 'COORDINADOR', NULL),
('Ana', 'García', 'C-002', 'FEMENINO', 'Calle Central 456', '1982-08-20', '555-0102', 'ana.garcia@example.com', NULL, true, 'COORDINADOR', NULL);


-- === SUPERVISORES (Asignados a los Coordinadores) ===

-- Supervisores de Carlos Rivera (id_superior = 1)
INSERT INTO agentes (nombre, apellido, numero_carnet, genero, direccion, fecha_nacimiento, telefono, email, ruta_fotografia, activo, rol, id_superior) VALUES
('Lucía', 'Martínez', 'S-101', 'FEMENINO', 'Calle Norte 789', '1988-11-10', '555-0201', 'lucia.martinez@example.com', NULL, true, 'SUPERVISOR', 1),
('Pedro', 'López', 'S-102', 'MASCULINO', 'Av. Sur 321', '1990-02-25', '555-0202', 'pedro.lopez@example.com', NULL, true, 'SUPERVISOR', 1);

-- Supervisores de Ana García (id_superior = 2)
INSERT INTO agentes (nombre, apellido, numero_carnet, genero, direccion, fecha_nacimiento, telefono, email, ruta_fotografia, activo, rol, id_superior) VALUES
('Sofía', 'Hernández', 'S-103', 'FEMENINO', 'Paseo Oeste 654', '1989-07-30', '555-0203', 'sofia.hernandez@example.com', NULL, true, 'SUPERVISOR', 2),
('Javier', 'Gómez', 'S-104', 'MASCULINO', 'Boulevard Este 987', '1991-04-05', '555-0204', 'javier.gomez@example.com', NULL, true, 'SUPERVISOR', 2);


-- === AGENTES DE SEGURIDAD (Asignados a los Supervisores) ===

-- Agentes de Lucía Martínez (id_superior = 3)
INSERT INTO agentes (nombre, apellido, numero_carnet, genero, direccion, fecha_nacimiento, telefono, email, ruta_fotografia, activo, rol, id_superior) VALUES
('Elena', 'Pérez', 'A-201', 'FEMENINO', 'Calle Falsa 1', '1995-01-10', '555-0301', 'elena.perez@example.com', NULL, true, 'AGENTE', 3),
('Miguel', 'Sánchez', 'A-202', 'MASCULINO', 'Calle Falsa 2', '1996-03-12', '555-0302', 'miguel.sanchez@example.com', NULL, true, 'AGENTE', 3),
('Laura', 'Ramírez', 'A-203', 'FEMENINO', 'Calle Falsa 3', '1997-05-14', '555-0303', 'laura.ramirez@example.com', NULL, true, 'AGENTE', 3),
('David', 'Torres', 'A-204', 'MASCULINO', 'Calle Falsa 4', '1998-07-16', '555-0304', 'david.torres@example.com', NULL, true, 'AGENTE', 3),
('Carmen', 'Flores', 'A-205', 'FEMENINO', 'Calle Falsa 5', '1999-09-18', '555-0305', 'carmen.flores@example.com', NULL, true, 'AGENTE', 3);

-- Agentes de Pedro López (id_superior = 4)
INSERT INTO agentes (nombre, apellido, numero_carnet, genero, direccion, fecha_nacimiento, telefono, email, ruta_fotografia, activo, rol, id_superior) VALUES
('Ricardo', 'Vargas', 'A-206', 'MASCULINO', 'Av. Inventada 1', '1995-02-20', '555-0306', 'ricardo.vargas@example.com', NULL, true, 'AGENTE', 4),
('Isabel', 'Rojas', 'A-207', 'FEMENINO', 'Av. Inventada 2', '1996-04-22', '555-0307', 'isabel.rojas@example.com', NULL, true, 'AGENTE', 4),
('Fernando', 'Molina', 'A-208', 'MASCULINO', 'Av. Inventada 3', '1997-06-24', '555-0308', 'fernando.molina@example.com', NULL, true, 'AGENTE', 4),
('Patricia', 'Castro', 'A-209', 'FEMENINO', 'Av. Inventada 4', '1998-08-26', '555-0309', 'patricia.castro@example.com', NULL, true, 'AGENTE', 4),
('Jorge', 'Ortiz', 'A-210', 'MASCULINO', 'Av. Inventada 5', '1999-10-28', '555-0310', 'jorge.ortiz@example.com', NULL, true, 'AGENTE', 4);

-- Agentes de Sofía Hernández (id_superior = 5)
INSERT INTO agentes (nombre, apellido, numero_carnet, genero, direccion, fecha_nacimiento, telefono, email, ruta_fotografia, activo, rol, id_superior) VALUES
('Mónica', 'Silva', 'A-211', 'FEMENINO', 'Paseo Imaginario 1', '1995-03-15', '555-0311', 'monica.silva@example.com', NULL, true, 'AGENTE', 5),
('Sergio', 'Reyes', 'A-212', 'MASCULINO', 'Paseo Imaginario 2', '1996-05-17', '555-0312', 'sergio.reyes@example.com', NULL, true, 'AGENTE', 5),
('Raquel', 'Mendoza', 'A-213', 'FEMENINO', 'Paseo Imaginario 3', '1997-07-19', '555-0313', 'raquel.mendoza@example.com', NULL, true, 'AGENTE', 5),
('Alejandro', 'Paredes', 'A-214', 'MASCULINO', 'Paseo Imaginario 4', '1998-09-21', '555-0314', 'alejandro.paredes@example.com', NULL, true, 'AGENTE', 5),
('Beatriz', 'León', 'A-215', 'FEMENINO', 'Paseo Imaginario 5', '1999-11-23', '555-0315', 'beatriz.leon@example.com', NULL, true, 'AGENTE', 5);

-- Agentes de Javier Gómez (id_superior = 6)
INSERT INTO agentes (nombre, apellido, numero_carnet, genero, direccion, fecha_nacimiento, telefono, email, ruta_fotografia, activo, rol, id_superior) VALUES
('Andrés', 'Soto', 'A-216', 'MASCULINO', 'Boulevard Ficticio 1', '1995-04-25', '555-0316', 'andres.soto@example.com', NULL, true, 'AGENTE', 6),
('Verónica', 'Romero', 'A-217', 'FEMENINO', 'Boulevard Ficticio 2', '1996-06-27', '555-0317', 'veronica.romero@example.com', NULL, true, 'AGENTE', 6),
('Óscar', 'Navarro', 'A-218', 'MASCULINO', 'Boulevard Ficticio 3', '1997-08-29', '555-0318', 'oscar.navarro@example.com', NULL, true, 'AGENTE', 6),
('Cristina', 'Guerrero', 'A-219', 'FEMENINO', 'Boulevard Ficticio 4', '1998-10-31', '555-0319', 'cristina.guerrero@example.com', NULL, true, 'AGENTE', 6),
('Mario', 'Vega', 'A-220', 'MASCULINO', 'Boulevard Ficticio 5', '1999-12-01', '555-0320', 'mario.vega@example.com', NULL, true, 'AGENTE', 6);