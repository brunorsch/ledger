CREATE TABLE meses (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_publico uuid NOT NULL,
    usuario_id bigint NOT NULL,
    mes_referencia date NOT NULL,
    captura_parametros jsonb NOT NULL,
    alternar_investir boolean NOT NULL DEFAULT true,
    alternar_lazer boolean NOT NULL DEFAULT true,
    deficit numeric(14,2) NOT NULL DEFAULT 0,
    criado_em timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uk_meses_id_publico UNIQUE (id_publico),
    CONSTRAINT fk_meses_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT ck_meses_captura_versao_esquema CHECK (captura_parametros ? 'versao_esquema')
);
CREATE INDEX idx_meses_usuario_id ON meses (usuario_id);