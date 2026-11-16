# Induztek — Backend de microservicios

Backend de Induztek App migrado a una arquitectura de microservicios con Spring Boot 4, Java 21, MySQL, Eureka, Spring Cloud Gateway, JWT y Docker Compose.

> **Estado de verificación:** el código y la estructura fueron generados y revisados estáticamente. En este entorno no se pudo ejecutar Maven/Docker ni compilar los proyectos, por lo que no se afirma que los servicios compilen o que la cobertura alcance el 80 %. Se incluyen pruebas unitarias y configuración JaCoCo para que puedas ejecutar la verificación en tu equipo.

## 1. Estructura

```text
backend/
├── eureka-server/       # Registro y descubrimiento de servicios (8761)
├── api-gateway/         # Entrada HTTP para Android (8090)
├── usuarios/            # Identidad, roles, permisos, sesión y cuentas (8081)
├── activos/             # Regiones, comunas, instalaciones y equipos (8082)
├── protocolos/          # Protocolos, pruebas, parámetros y mediciones (8083)
├── mysql/init/          # Creación de bases de datos independientes
├── docker-compose.yml
├── .env.example
└── pruebas-api.http
```

## 2. Límites de los microservicios y propiedad de datos

| Servicio | Base de datos | Tablas de su propiedad |
|---|---|---|
| Usuarios | `induztek_usuarios` | `usuarios`, `rol`, `permiso`, `rol_permiso`, `token_acceso` |
| Activos | `induztek_activos` | `region`, `comuna`, `tipo_equipo`, `instalacion`, `equipo` |
| Protocolos | `induztek_protocolos` | `estado_protocolo`, `tipo_prueba`, `tipo_prueba_equipo`, `parametro`, `protocolo`, `prueba`, `medicion` |

Cada servicio utiliza exclusivamente sus repositorios y su propia base de datos. No se comparten tablas ni se ejecutan consultas SQL sobre la base de otro microservicio. Además, cada servicio se conecta con un usuario MySQL diferente (`usuarios_app`, `activos_app`, `protocolos_app`) que solo tiene privilegios sobre su propia base.

Los identificadores externos (`id_usuario_cliente`, `id_usuario_registra`, `id_equipo` e `id_tipo_equipo`) son referencias por ID, no claves foráneas entre bases. Se validan mediante endpoints REST internos con `WebClient` y `X-Internal-Token`.

`tipo_prueba_equipo.id_tipo_equipo` es un identificador externo: no tiene FK hacia Activos. `tipo_prueba_equipo.id_tipo_prueba` sí pertenece al dominio Protocolos y mantiene su FK local.

## 3. Roles y permisos

Los códigos de rol expuestos por la API son:

- `ADMIN`
- `CLIENTE`
- `TECNICO_INGENIERO`

El valor heredado `TECNICO` se transforma a `TECNICO_INGENIERO` durante la migración. Android utiliza el código oficial nuevo.

Permisos trasladados del proyecto Android original:

- `LEER`
- `CREACION`
- `EDICION`
- `FORMALIZACION`
- `ELIMINACION`
- `AGREGAR_PERSONAL`

El JWT contiene rol y permisos. Los servicios validan firma, expiración, rol y autoridades `PERM_*`. Las operaciones internas no reciben un JWT de usuario: requieren el token interno compartido.

## 4. Migración de las cuentas SQLite

La carga inicial conserva los datos de las **13 cuentas originales** del `InduztekDbHelper.kt`. Las cuentas que ya existían en MS Usuarios conservan su ID; las demás se crean con esos datos.

Como los hashes originales de SQLite son SHA-256 y no son credenciales BCrypt compatibles, cada cuenta migrada recibe un hash BCrypt aleatorio inutilizable para iniciar sesión y queda con `activo=false` y `debeActivar=true`. Se crea un token de invitación para definir una contraseña nueva.

La migración es idempotente: una cuenta que ya tiene su relación con `ROL` no se vuelve a desactivar ni se reinicia su contraseña en cada arranque. La cuenta administrativa de demostración existente se conserva y no forma parte de las 13 cuentas SQLite. En una base de datos nueva se crea `admin@induztek.cl` con contraseña inicial `Abc1234@`; cámbiala inmediatamente después del primer acceso.

### Activación en desarrollo sin SMTP

Con `MAIL_ENABLED=false` y `EXPOSE_DEV_TOKENS=true`, los tokens se registran en los logs de Usuarios. Puedes consultarlos con:

```bash
docker compose logs usuarios
```

Busca `Token de activación de migración`. En la app Android utiliza la pantalla de activación para definir una contraseña nueva. En producción configura SMTP y usa `EXPOSE_DEV_TOKENS=false`.

Las instalaciones, equipos, tipos, parámetros, compatibilidades y protocolos de demostración se cargan en las bases de datos propietarias. Las referencias a usuarios/equipos se resuelven consultando a los servicios propietarios.

## 5. Requisitos

- Java 21.
- Maven 3.9 o superior.
- Docker Desktop con Docker Compose v2.
- Android Studio para compilar la aplicación Android.

## 6. Configuración Docker

Desde esta carpeta:

```bash
cp .env.example .env
```

Edita `.env` y cambia al menos `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`, `JWT_SECRET` e `INTERNAL_SERVICE_TOKEN`. La misma `JWT_SECRET` debe utilizarse en Usuarios, Activos y Protocolos.

Inicia la infraestructura:

```bash
docker compose up --build
```

Servicios y puertos:

| Componente | URL |
|---|---|
| Eureka | http://localhost:8761 |
| API Gateway | http://localhost:8090 |
| Usuarios | http://localhost:8081 |
| Activos | http://localhost:8082 |
| Protocolos | http://localhost:8083 |
| MySQL | localhost:3306 |

Swagger:

- Usuarios: http://localhost:8081/swagger-ui/index.html
- Activos: http://localhost:8082/swagger-ui/index.html
- Protocolos: http://localhost:8083/swagger-ui/index.html

Las rutas de negocio de Android deben utilizar el Gateway (8090), no los puertos internos de cada servicio.

### Si ya existe un volumen MySQL antiguo

Los scripts de `mysql/init` se ejecutan únicamente cuando MySQL inicializa un directorio de datos vacío. Si conservas un volumen previo, crea las bases faltantes sin eliminar el volumen ni los datos:

```sql
CREATE DATABASE IF NOT EXISTS induztek_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS induztek_activos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS induztek_protocolos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'usuarios_app'@'%' IDENTIFIED BY 'CambiaClaveUsuarios';
CREATE USER IF NOT EXISTS 'activos_app'@'%' IDENTIFIED BY 'CambiaClaveActivos';
CREATE USER IF NOT EXISTS 'protocolos_app'@'%' IDENTIFIED BY 'CambiaClaveProtocolos';
ALTER USER 'usuarios_app'@'%' IDENTIFIED BY 'CambiaClaveUsuarios';
ALTER USER 'activos_app'@'%' IDENTIFIED BY 'CambiaClaveActivos';
ALTER USER 'protocolos_app'@'%' IDENTIFIED BY 'CambiaClaveProtocolos';
GRANT ALL PRIVILEGES ON induztek_usuarios.* TO 'usuarios_app'@'%';
GRANT ALL PRIVILEGES ON induztek_activos.* TO 'activos_app'@'%';
GRANT ALL PRIVILEGES ON induztek_protocolos.* TO 'protocolos_app'@'%';
FLUSH PRIVILEGES;
```

No ejecutes `docker compose down -v` si necesitas conservar la base de datos anterior.

## 7. Ejecución local sin Docker

Primero inicia MySQL y crea las tres bases indicadas. En terminales separadas:

```bash
cd eureka-server && mvn spring-boot:run
cd usuarios && mvn spring-boot:run
cd activos && mvn spring-boot:run
cd protocolos && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

Define en el entorno de cada proceso:

- `JWT_SECRET`: la misma clave de al menos 32 bytes en los tres microservicios.
- `INTERNAL_SERVICE_TOKEN`: el mismo token en Usuarios, Activos y Protocolos.
- Usuarios: `DB_URL`, `DB_USERNAME=usuarios_app`, `DB_PASSWORD=DB_USUARIOS_PASSWORD`.
- Activos: `DB_URL`, `DB_USERNAME=activos_app`, `DB_PASSWORD=DB_ACTIVOS_PASSWORD`.
- Protocolos: `DB_URL`, `DB_USERNAME=protocolos_app`, `DB_PASSWORD=DB_PROTOCOLS_PASSWORD`.
- `USUARIOS_URL` en Activos y Protocolos.
- `ACTIVOS_URL` en Protocolos.

## 8. Endpoints principales

Todas las rutas de negocio se exponen mediante `http://localhost:8090`.

### Usuarios

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/activar` | Público con token de invitación |
| POST | `/api/auth/recuperar` | Público |
| POST | `/api/auth/restablecer` | Público con token |
| POST | `/api/usuarios` | ADMIN + `AGREGAR_PERSONAL` |
| GET | `/api/usuarios/me` | Usuario autenticado |
| PUT | `/api/usuarios/me` | Propietario del perfil |
| PUT | `/api/usuarios/me/contrasena` | Propietario del perfil |
| PATCH | `/api/usuarios/{id}/estado` | ADMIN |

### Activos

| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/activos/regiones` | `LEER` |
| GET | `/api/activos/comunas` | `LEER` |
| GET | `/api/activos/tipos-equipo` | `LEER` |
| GET | `/api/activos/clientes` | TECNICO_INGENIERO + `LEER` |
| POST | `/api/activos/instalaciones` | TECNICO_INGENIERO + `CREACION` |
| GET | `/api/activos/clientes/{id}/instalaciones` | Propietario o personal autorizado |
| POST | `/api/activos/equipos` | TECNICO_INGENIERO + `CREACION` |
| GET | `/api/activos/instalaciones/{id}/equipos` | Propietario o personal autorizado |
| GET | `/api/activos/equipos/{id}` | Propietario o personal autorizado |

### Protocolos

| Método | Ruta | Acceso |
|---|---|---|
| GET | `/api/protocolos/catalogos/tipos-prueba?idTipoEquipo={id}` | `LEER` |
| GET | `/api/protocolos/catalogos/tipos-prueba/{id}/parametros` | `LEER` |
| POST | `/api/protocolos/borradores` | TECNICO_INGENIERO + `CREACION` |
| PUT | `/api/protocolos/{id}/borrador` | TECNICO_INGENIERO + `EDICION` |
| POST | `/api/protocolos/{id}/pruebas/seleccionadas` | TECNICO_INGENIERO + `EDICION` |
| POST/PUT | `/api/protocolos/{id}/pruebas` | TECNICO_INGENIERO + `EDICION` |
| PUT | `/api/protocolos/{id}/estado` | Permiso según estado destino |
| POST | `/api/protocolos/{id}/preparar-edicion` | Creador + `EDICION` |
| POST | `/api/protocolos/{id}/reemplazar` | TECNICO_INGENIERO + `CREACION` + `ELIMINACION` |
| GET | `/api/protocolos/historial` | TECNICO_INGENIERO + `LEER` |
| GET | `/api/protocolos/equipos/{id}` | Propietario o TECNICO_INGENIERO |
| GET | `/api/protocolos/clientes/me` | CLIENTE + `LEER` |

## 9. Pruebas unitarias y cobertura

Los tres microservicios incluyen actualmente **24 pruebas unitarias** con JUnit 5 y Mockito, además de configuración JaCoCo para generar los reportes por servicio.

Desde cada carpeta (`usuarios`, `activos`, `protocolos`):

```bash
mvn clean test
```

Genera el reporte JaCoCo con:

```bash
mvn clean test
```

Abre `target/site/jacoco/index.html`.

No se fija un umbral de `jacoco:check` para evitar que una cobertura no medida bloquee la compilación. Ejecuta los comandos y revisa el porcentaje real antes de entregar. Los tests incluidos son una base inicial, no una afirmación de que ya se alcanzó el 80 %.

## 10. Notas de implementación

- Las respuestas de seguridad usan HTTP 401/403; las validaciones y reglas de negocio utilizan los códigos HTTP correspondientes.
- Los servicios no se llaman mediante SQL compartido. Activos consulta Usuarios; Protocolos consulta Activos y Usuarios.
- Las rutas internas `/api/internal/**` no se deben exponer a Internet. El token `X-Internal-Token` debe ser secreto y distinto en producción.
- El avatar conserva el mecanismo de almacenamiento de archivos del backend existente y expone su URL; no se comparte la tabla ni la base de datos con otro servicio.
- Los archivos `application-*.properties` mantienen configuración separada para ejecución local y Docker.
