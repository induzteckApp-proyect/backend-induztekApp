#!/bin/bash
set -euo pipefail

mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<SQL
CREATE DATABASE IF NOT EXISTS induztek_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS induztek_activos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS induztek_protocolos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'usuarios_app'@'%' IDENTIFIED BY '${DB_USUARIOS_PASSWORD}';
CREATE USER IF NOT EXISTS 'activos_app'@'%' IDENTIFIED BY '${DB_ACTIVOS_PASSWORD}';
CREATE USER IF NOT EXISTS 'protocolos_app'@'%' IDENTIFIED BY '${DB_PROTOCOLS_PASSWORD}';

GRANT ALL PRIVILEGES ON induztek_usuarios.* TO 'usuarios_app'@'%';
GRANT ALL PRIVILEGES ON induztek_activos.* TO 'activos_app'@'%';
GRANT ALL PRIVILEGES ON induztek_protocolos.* TO 'protocolos_app'@'%';
FLUSH PRIVILEGES;
SQL
