CREATE TABLE usuarios (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    identity_provider varchar(255),
    criado_em timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uk_usuarios_identity_provider UNIQUE (identity_provider)
);