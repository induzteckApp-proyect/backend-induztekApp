# Induztek — Backend

Backend de Induztek App con Java 21, Spring Boot, MySQL, Eureka, API Gateway, JWT y Docker Compose.

## Requisitos

- Java 21
- Maven 3.9+
- MySQL 8 mediante Laragon o Docker
- Docker Desktop y Docker Compose si se utilizará el entorno Docker

## Microservicios y puertos

| Componente | Puerto | URL |
|---|---:|---|
| Eureka Server | 8761 | http://localhost:8761 |
| API Gateway | 8090 | http://localhost:8090 |
| Usuarios | 8081 | http://localhost:8081 |
| Activos | 8082 | http://localhost:8082 |
| Protocolos | 8083 | http://localhost:8083 |
| MySQL | 3306 | localhost:3306 |

Bases de datos independientes: `induztek_usuarios`, `induztek_activos` e `induztek_protocolos`.

## Ejecución local con Laragon

1. Inicia MySQL desde Laragon.
2. Crea las tres bases de datos anteriores con codificación `utf8mb4`.
3. Configura las variables de entorno en las terminales:
   - `JWT_SECRET`: mismo secreto aleatorio de al menos 32 bytes para Usuarios, Activos y Protocolos.
   - `INTERNAL_SERVICE_TOKEN`: mismo token interno para Usuarios, Activos y Protocolos.
   - `MAIL_ENABLED=true` para activar envío de correo.
   - `MAIL_USERNAME`, `MAIL_PASSWORD` y `MAIL_FROM` con la cuenta SMTP y su contraseña de aplicación.
4. En ejecución local, si Laragon utiliza `root` sin contraseña, los valores predeterminados de conexión ya están configurados.
5. Inicia los proyectos en terminales separadas y en este orden:

```bash
cd eureka-server
mvn spring-boot:run
```

```bash
cd usuarios
mvn spring-boot:run
```

```bash
cd activos
mvn spring-boot:run
```

```bash
cd protocolos
mvn spring-boot:run
```

```bash
cd api-gateway
mvn spring-boot:run
```

Activos consulta Usuarios al iniciar para asociar las instalaciones semilla a los clientes. Por eso Usuarios debe estar iniciado primero.

## Ejecución con Docker

1. Copia `.env.example` como `.env`.
2. Define contraseñas seguras para `MYSQL_ROOT_PASSWORD`, `DB_USUARIOS_PASSWORD`, `DB_ACTIVOS_PASSWORD` y `DB_PROTOCOLS_PASSWORD`.
3. Define `JWT_SECRET` e `INTERNAL_SERVICE_TOKEN` con valores aleatorios seguros.
4. Configura las variables SMTP si se enviarán correos.
5. Ejecuta:

```bash
docker compose up --build
```

Para detener los servicios sin borrar las bases de datos:

```bash
docker compose down
```

No utilices `docker compose down -v` si necesitas conservar los datos.

## Swagger y documentación de API

- Usuarios: http://localhost:8081/swagger-ui/index.html
- Activos: http://localhost:8082/swagger-ui/index.html
- Protocolos: http://localhost:8083/swagger-ui/index.html
- Eureka: http://localhost:8761
- Gateway: http://localhost:8090

Android debe comunicarse con el Gateway en el puerto `8090`. Las rutas públicas de negocio son:

### Usuarios

- `POST /api/auth/login`
- `POST /api/auth/activar`
- `POST /api/auth/recuperar`
- `POST /api/auth/restablecer`
- `POST /api/usuarios`
- `GET /api/usuarios/me`
- `PUT /api/usuarios/me`
- `PUT /api/usuarios/me/contrasena`
- `POST /api/usuarios/me/avatar`
- `GET /api/usuarios`
- `GET /api/usuarios/{id}`
- `PATCH /api/usuarios/{id}/estado`
- `GET /api/internal/usuarios`
- `GET /api/internal/usuarios/{id}`

### Activos

- `GET /api/activos/regiones`
- `GET /api/activos/comunas`
- `GET /api/activos/tipos-equipo`
- `GET /api/activos/clientes`
- `GET /api/activos/clientes/{idCliente}/instalaciones`
- `POST /api/activos/instalaciones`
- `GET /api/activos/instalaciones/{id}`
- `GET /api/activos/instalaciones/{idInstalacion}/equipos`
- `POST /api/activos/equipos`
- `GET /api/activos/equipos/{id}`
- `GET /api/activos/internal/tipos-equipo`
- `GET /api/activos/internal/tipos-equipo/{id}`
- `GET /api/activos/internal/equipos?tag=...`
- `GET /api/activos/internal/equipos/{id}`

### Protocolos

- `GET /api/protocolos/catalogos/tipos-prueba`
- `GET /api/protocolos/catalogos/tipos-prueba/{idTipoPrueba}/parametros`
- `POST /api/protocolos/registro-inmediato`
- `POST /api/protocolos/borradores`
- `GET /api/protocolos/borradores/me`
- `PUT /api/protocolos/{id}/borrador`
- `POST /api/protocolos/{id}/pruebas/seleccionadas`
- `POST /api/protocolos/{id}/pruebas`
- `PUT /api/protocolos/{id}/estado`
- `POST /api/protocolos/{id}/preparar-edicion`
- `POST /api/protocolos/{id}/reemplazar`
- `GET /api/protocolos/historial`
- `GET /api/protocolos/equipos/{idEquipo}`
- `GET /api/protocolos/clientes/me`
- `GET /api/protocolos/{id}`

## Cuentas de demostración

Las 14 cuentas semilla quedan activas (`activo=true`, `debeActivar=false`) y utilizan la contraseña `Abc1234@`, almacenada con BCrypt. Esto aplica exclusivamente al entorno académico/de demostración.

- Administrador: `admin@induztek.cl`
- Técnico principal: `tecnico@induztek.cl`
- Cliente principal: `cliente@ejemplo.cl`
- Clientes adicionales: `jarancibia@svcentral.ejemplo`, `pnavarro@cerroalto.ejemplo`, `cmendez@losaromos.ejemplo`, `lbravo@lomasblancas.ejemplo`, `fordenes@andesnorte.ejemplo`
- Técnicos adicionales: `jtapia@induztek.ejemplo`, `crondon@induztek.ejemplo`, `dvalenzuela@induztek.ejemplo`, `acontreras@induztek.ejemplo`, `smorales@induztek.ejemplo`, `respinoza@induztek.ejemplo`

Los datos semilla se sincronizan al iniciar Usuarios. Las cuentas de demostración conservan el rol y los datos comerciales correspondientes.

## Activación y recuperación por correo

El correo contiene un código de activación o recuperación que el usuario debe copiar en la aplicación. No se envían enlaces web.

Variables SMTP principales:

- `MAIL_ENABLED=true`
- `MAIL_HOST=smtp.gmail.com`
- `MAIL_PORT=587`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`
- `MAIL_SMTP_AUTH=true`
- `MAIL_SMTP_STARTTLS=true`

Si el envío falla después de crear una cuenta, el backend informa que la cuenta ya fue creada y que no debe registrarse nuevamente. No devuelve códigos de activación en la respuesta de la API. Si SMTP está deshabilitado, la API informa que no se envió el correo y no expone el código en la respuesta, los logs ni la aplicación.

## Pruebas unitarias

En cada microservicio:

```bash
mvn clean test
```

El reporte JaCoCo queda en `target/site/jacoco/index.html`. La cobertura debe revisarse en el equipo donde se ejecuten las pruebas; no se declara un porcentaje sin medirlo.
