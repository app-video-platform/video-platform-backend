-- Keep one deterministic role per user before enforcing the invariant.
WITH ranked_roles AS (
    SELECT ur.user_id,
           ur.role_id,
           ROW_NUMBER() OVER (
               PARTITION BY ur.user_id
               ORDER BY CASE r.role_name
                            WHEN 'ADMIN' THEN 1
                            WHEN 'CREATOR' THEN 2
                            WHEN 'USER' THEN 3
                            ELSE 4
                        END,
                        ur.role_id
           ) AS role_rank
    FROM user_roles ur
    JOIN roles r ON r.role_id = ur.role_id
)
DELETE FROM user_roles
WHERE (user_id, role_id) IN (
    SELECT user_id, role_id
    FROM ranked_roles
    WHERE role_rank > 1
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u
JOIN roles r ON r.role_name = 'USER'
WHERE NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.user_id
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_roles_user_id
    ON user_roles(user_id);
