-- Actualiza la restriccion creada en bases existentes para admitir todos los estados.
ALTER TABLE incidencias
    DROP CONSTRAINT IF EXISTS incidencias_estado_check;

ALTER TABLE incidencias
    DROP CONSTRAINT IF EXISTS ck_incidencias_estado;

ALTER TABLE incidencias
    ADD CONSTRAINT ck_incidencias_estado
        CHECK (estado IS NULL OR estado IN ('ABIERTO', 'CERRADO', 'EN_PROGRESO', 'RESUELTO'));
