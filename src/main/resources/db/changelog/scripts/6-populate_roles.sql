INSERT INTO roles (role_name)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ADMIN' OR role_name = 'admin');

INSERT INTO roles (role_name)
SELECT 'USER'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'USER' OR role_name = 'user');
