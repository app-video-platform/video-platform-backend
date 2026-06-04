UPDATE roles
SET role_name = 'ADMIN'
WHERE LOWER(role_name) = 'admin';

UPDATE roles
SET role_name = 'USER'
WHERE LOWER(role_name) = 'user';

INSERT INTO roles (role_name)
SELECT 'CREATOR'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'CREATOR');
