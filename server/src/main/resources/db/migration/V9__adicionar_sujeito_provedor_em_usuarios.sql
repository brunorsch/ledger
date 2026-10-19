ALTER TABLE usuarios DROP CONSTRAINT uk_usuarios_identity_provider;

ALTER TABLE usuarios ADD COLUMN sujeito_provedor varchar(255);

CREATE UNIQUE INDEX uk_usuarios_sujeito_provedor ON usuarios (sujeito_provedor);
