

  -- Asegúrate de estar conectado a tu base de datos 'aeroseguridad_db'
-- Inserta las posiciones que necesites. El ID suele ser autogenerado.
-- Ajusta genero_requerido y requiere_entrenamiento_especial según tus reglas.

INSERT INTO posiciones_seguridad (nombre_posicion, descripcion, genero_requerido, requiere_entrenamiento_especial) VALUES
('Rampa', 'Tareas generales en rampa', 'MASCULINO', false),
('Bodega', 'Manejo de carga en bodega', 'CUALQUIERA', true),
('BDP', 'Agente Bilingüe Documentación/Pasajeros', 'CUALQUIERA', true),
('Control Acceso', 'Verificación en puntos de acceso', 'CUALQUIERA', false),
('Scanner Equipaje', 'Operador de máquina de rayos X para equipaje', 'CUALQUIERA', true),
('Scanner Pasajeros', 'Operador de scanner corporal', 'CUALQUIERA', true);