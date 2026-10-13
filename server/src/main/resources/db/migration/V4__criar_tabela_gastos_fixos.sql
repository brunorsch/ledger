CREATE TABLE gastos_fixos (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_publico uuid NOT NULL,
    usuario_id bigint NOT NULL,
    descricao text NOT NULL,
    valor_padrao numeric(14,2) NOT NULL,
    ativo boolean NOT NULL DEFAULT true,
    criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uk_gastos_fixos_id_publico UNIQUE (id_publico),
    CONSTRAINT fk_gastos_fixos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);
CREATE INDEX idx_gastos_fixos_usuario_id ON gastos_fixos (usuario_id);