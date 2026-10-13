CREATE TYPE tipo_lancamento AS ENUM ('DESPESA', 'RECEITA');
CREATE TABLE lancamentos (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_publico uuid NOT NULL,
    usuario_id bigint NOT NULL,
    mes_id bigint NOT NULL,
    tipo tipo_lancamento NOT NULL,
    descricao text NOT NULL,
    valor numeric(14,2) NOT NULL,
    pago boolean NOT NULL DEFAULT false,
    pago_em timestamptz,
    criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT uk_lancamentos_id_publico UNIQUE (id_publico),
    CONSTRAINT fk_lancamentos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_lancamentos_mes FOREIGN KEY (mes_id) REFERENCES meses (id)
);
CREATE INDEX idx_lancamentos_usuario_id ON lancamentos (usuario_id);
CREATE INDEX idx_lancamentos_mes_id ON lancamentos (mes_id);