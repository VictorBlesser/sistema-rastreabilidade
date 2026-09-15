INSERT INTO usuario_perfil (usuario_id, perfil_id)
SELECT u.id, p.id
FROM usuario u
JOIN perfil_acesso p ON p.codigo = u.perfil
WHERE NOT EXISTS (
    SELECT 1
    FROM usuario_perfil up
    WHERE up.usuario_id = u.id
);