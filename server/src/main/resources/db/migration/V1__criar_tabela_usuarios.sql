CREATE TABLE usuarios (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sujeito_provedor varchar(255),
    criado_em timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_sujeito_provedor UNIQUE (sujeito_provedor)
);