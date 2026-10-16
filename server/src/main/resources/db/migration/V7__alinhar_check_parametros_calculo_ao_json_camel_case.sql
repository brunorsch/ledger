ALTER TABLE meses DROP CONSTRAINT ck_meses_parametros_calculo_versao_esquema;
ALTER TABLE meses ADD CONSTRAINT ck_meses_parametros_calculo_versao_esquema CHECK (parametros_calculo ? 'versaoEsquema');
