DO $$
DECLARE
    unique_constraint RECORD;
BEGIN
    FOR unique_constraint IN
        SELECT constraint_definition.conname
        FROM pg_constraint constraint_definition
        WHERE constraint_definition.conrelid = to_regclass('incidencias')
          AND constraint_definition.contype = 'u'
          AND constraint_definition.conkey = ARRAY[
              (
                  SELECT column_definition.attnum
                  FROM pg_attribute column_definition
                  WHERE column_definition.attrelid = constraint_definition.conrelid
                    AND column_definition.attname = 'codigo_incidencia'
                    AND NOT column_definition.attisdropped
              )
          ]::smallint[]
    LOOP
        EXECUTE format('ALTER TABLE incidencias DROP CONSTRAINT %I', unique_constraint.conname);
    END LOOP;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = to_regclass('incidencias')
          AND conname = 'uk_incidencia_proyecto_codigo'
    ) THEN
        ALTER TABLE incidencias
            ADD CONSTRAINT uk_incidencia_proyecto_codigo
                UNIQUE (id_proyecto, codigo_incidencia);
    END IF;
END
$$;
