INSERT INTO usuario (
    nome,
    login,
    senha_hash,
    perfil,
    ativo
)
VALUES (
    'Administrador',
    'admin',
    '{bcrypt}$2a$10$JmUBVxI05ERpwfLh4A3Xm..P0KJ7TcEEbCMInEygWmJk8KqzkDy3e',
    'ADMINISTRADOR',
    TRUE
);