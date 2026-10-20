ALTER TABLE proyectos
    ADD COLUMN IF NOT EXISTS id_administrador BIGINT;

ALTER TABLE proyectos
    ADD COLUMN IF NOT EXISTS eliminado BOOLEAN NOT NULL DEFAULT FALSE;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_proyectos_administrador'
          AND conrelid = 'proyectos'::regclass
    ) THEN
        ALTER TABLE proyectos
            ADD CONSTRAINT fk_proyectos_administrador
                FOREIGN KEY (id_administrador)
                REFERENCES usuarios (id_usuario);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM proyectos
        WHERE id_administrador IS NULL
    ) THEN
        ALTER TABLE proyectos
            ALTER COLUMN id_administrador SET NOT NULL;
    END IF;
END $$;
