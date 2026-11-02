-- A V5 grava o usuário semente com id explícito (OVERRIDING SYSTEM VALUE), o que
-- não avança a sequência identity: o primeiro usuário novo colidiria com o id 1.
SELECT setval(pg_get_serial_sequence('usuarios', 'id'), (SELECT max(id) FROM usuarios));
