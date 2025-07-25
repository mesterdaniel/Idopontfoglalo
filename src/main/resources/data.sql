INSERT INTO roles (id, role_name) VALUES (1, 'ROLE_ADMIN');
INSERT INTO roles (id, role_name) VALUES (2, 'ROLE_USER');

INSERT INTO users (id, username, password, enabled) VALUES (1, 'admin', '{noop}admin123', true);
INSERT INTO users (id, username, password, enabled) VALUES (2, 'user', '{noop}user123', true);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1); -- admin → ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2); -- user → ROLE_USER