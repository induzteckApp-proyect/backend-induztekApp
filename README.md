# Induztek — Backend de Usuarios, Eureka y API Gateway

## Qué contiene

- `usuarios/`: API REST de usuarios, perfiles, autenticación JWT, roles, activación por invitación, recuperación y cambio de contraseña, validaciones, BCrypt, manejo global de errores y Swagger.
- `eureka-server/`: registro y descubrimiento de servicios, reutilizando la configuración del proyecto de ejemplo.
- `api-gateway/`: punto de entrada HTTP y ruta hacia `USUARIOS`.
- `docker-compose.yml`: MySQL 8.4 + Eureka + Usuarios + Gateway.

Los roles del proyecto son exactamente `ADMIN`, `TECNICO_INGENIERO` y `CLIENTE`.

## Dependencias del proyecto e instrucciones de ejecución:

   - Project: Maven
   - Language: Java
   - Spring Boot: `4.0.9`
   - Group: `com.induztek`
   - Artifact: `usuarios`
   - Name: `usuarios`
   - Package name: `com.induztek.usuarios`
   - Packaging: Jar
   - Java: `21`
3. Selecciona estas dependencias:
   - Spring Web MVC
   - Spring Data JPA
   - Validation
   - Spring Security
   - Java Mail Sender
   - MySQL Driver
   - Eureka Discovery Client
   - Spring Boot Actuator
4. Dentro del POM: JJWT y Springdoc Swagger, que no son dependencias base del Initializr.
5. Abre el proyecto `usuarios` como proyecto Maven y espera que descargue las dependencias.
6. Crea la base `induztek_usuarios` en MySQL (o deja que Docker Compose la cree).
7. Configura `application.properties` para Laragon o usa `application-docker.properties` dentro de Docker.
8. Ejecuta `UsuariosApplication.java`.


## Requisitos

- JDK 21
- Maven 3.9+
- Docker Desktop con Docker Compose (opción recomendada)
- Para ejecución local con Laragon: MySQL iniciado y base `induztek_usuarios` creada.

El proyecto usa Spring Boot 4.0.9 y Spring Cloud 2025.1.4. La combinación pertenece a la misma línea de compatibilidad 4.0.x / 2025.1.x. Springdoc 3.0.3 se utiliza para Spring Boot 4.0.x.

## Opción A — Levantar todo con Docker (recomendado)

1. Abre una terminal en esta carpeta (`backend`).
2. Copia `.env.example` a `.env`.
3. Cambia `JWT_SECRET` por una cadena propia de al menos 32 bytes. No subas `.env` a Git.
4. Para la primera prueba deja `MAIL_ENABLED=false` y `EXPOSE_DEV_TOKENS=true`. Así el microservicio entrega el token de invitación/recuperación en `tokenDesarrollo`, porque los correos ficticios no pueden recibir mensajes.
5. Construye los JAR dentro de cada imagen (Dockerfile multi-stage) e inicia los servicios:

   ```bash
   docker compose up --build -d
   ```

6. Revisa el estado:

   ```bash
   docker compose ps
   docker compose logs -f usuarios
   ```

7. Abre:
   - Eureka: http://localhost:8761
   - Swagger directo: http://localhost:8081/swagger-ui.html
   - Swagger vía Gateway: http://localhost:8090/swagger-ui.html
   - Gateway base: http://localhost:8090

8. En Eureka debe aparecer `USUARIOS` y `API-GATEWAY` como `UP`.

Para detener:
```bash
docker compose down
```
Para detener y borrar también los datos de la BD (¡esto elimina usuarios!):
```bash
docker compose down -v
```

## Opción B — Ejecutar con Laragon (MySQL local)

1. Inicia MySQL desde Laragon.
2. En HeidiSQL/phpMyAdmin crea la base:

   ```sql
   CREATE DATABASE induztek_usuarios
   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. En tres terminales distintas ejecuta:
   - `eureka-server`: `mvn spring-boot:run`
   - `usuarios`: `mvn spring-boot:run`
   - `api-gateway`: `mvn spring-boot:run`

4. En Laragon normalmente el usuario local es `root` sin contraseña. Si tu configuración es distinta, define antes de ejecutar:
   - Windows PowerShell: `$env:DB_USERNAME="root"; $env:DB_PASSWORD=""`
   - Linux/macOS: `export DB_USERNAME=root DB_PASSWORD=`

5. `application.properties` usa `localhost:3306`, Eureka `localhost:8761`, Usuarios `8081` y Gateway `8090`.
6. Hibernate usa `ddl-auto=update`: crea/actualiza tablas dentro de la base existente. No crea por sí solo el esquema MySQL, por eso se crea `induztek_usuarios` en el paso 2.

## Cuentas de prueba

El `SeedData` crea las cuentas solo si el correo no existe:

| Correo | Rol | Contraseña de desarrollo |
|---|---|---|
| tecnico@induztek.cl | TECNICO_INGENIERO | Abc1234@ |
| cliente@ejemplo.cl | CLIENTE | Abc1234@ |
| admin@induztek.cl | ADMIN | Abc1234@ |

Son datos ficticios de evaluación. La contraseña compartida es únicamente para desarrollo académico. No usarla en producción. Los RUT de prueba son ficticios y solo satisfacen el algoritmo de verificación.

## Seguridad entre Gateway y microservicio

El Gateway enruta y conserva el encabezado `Authorization`; el microservicio Usuarios valida la firma/expiración del JWT y aplica los permisos por rol. Por eso, incluso si se llama directamente al puerto `8081`, los endpoints privados siguen protegidos. No se confía en ocultar botones en Android como mecanismo de seguridad.

Pruebas rápidas de autorización:
- Sin JWT, `GET /api/usuarios/me` debe responder `401`.
- Con JWT de CLIENTE, `POST /api/usuarios` debe responder `403`.
- Con JWT de ADMIN, `POST /api/usuarios` debe responder `201`.
- Con JWT de usuario, `PUT /api/usuarios/me` debe actualizar solo el perfil asociado al token.

## Endpoints principales

Base local: `http://localhost:8090` (Gateway) o `http://localhost:8081` (Usuarios directo).

| Método | Ruta | Acceso | Función |
|---|---|---|---|
| POST | `/api/auth/login` | Público | Autenticación y JWT |
| POST | `/api/auth/activar` | Público + token | Activar cuenta y definir contraseña |
| POST | `/api/auth/recuperar` | Público | Solicitar token de recuperación |
| POST | `/api/auth/restablecer` | Público + token | Definir nueva contraseña |
| POST | `/api/usuarios` | ADMIN | Crear cuenta para cualquier rol |
| GET | `/api/usuarios` | ADMIN | Listar usuarios |
| GET | `/api/usuarios/me` | Autenticado | Consultar perfil propio |
| PUT | `/api/usuarios/me` | Autenticado | Actualizar campos permitidos del perfil |
| PUT | `/api/usuarios/me/contrasena` | Autenticado | Cambiar contraseña validando la actual |
| POST | `/api/usuarios/me/avatar` | Autenticado | Subir avatar JPG/PNG/WEBP (máx. 3 MB) |
| GET | `/api/usuarios/{id}` | ADMIN o dueño | Consultar usuario |

### Contrato de login

```json
{
  "correo": "admin@induztek.cl",
  "contrasena": "Abc1234@"
}
```

La respuesta contiene `token`, `tipo` y `usuario`. En las demás llamadas autenticadas agrega:
`Authorization: Bearer <token>`.

### Crear una cuenta como ADMIN

Primero autentícate con `admin@induztek.cl`. Después envía el JWT en Authorization:

```json
{
  "rut": "12345678-5",
  "nombre": "María",
  "apellido": "Pérez",
  "correo": "maria@ejemplo.cl",
  "telefono": "56912345678",
  "rol": "CLIENTE",
  "giro": "Servicios eléctricos",
  "contactoNombre": "María Pérez",
  "contactoCargo": "Administradora"
}
```

La cuenta queda pendiente de activación. Con `MAIL_ENABLED=false`, la respuesta trae `tokenDesarrollo`. En Swagger prueba `POST /api/auth/activar`:

```json
{
  "token": "TOKEN_DEVUELTO_POR_CREAR",
  "nuevaContrasena": "Abc1234@"
}
```

Con SMTP configurado, el código de un solo uso se envía al correo y `tokenDesarrollo` no se expone. El usuario lo escribe en la pantalla **Activar cuenta** o **Recuperar contraseña** de Android.

### Recuperar contraseña

`POST /api/auth/recuperar`:
```json
{ "correo": "tecnico@induztek.cl" }
```
En modo desarrollo, copia `tokenDesarrollo` y llama `POST /api/auth/restablecer`:
```json
{ "token": "TOKEN_DEVUELTO", "nuevaContrasena": "NuevaClave9@" }
```
Si el correo no existe, el servicio responde `404`. Para producción se recomienda cambiar esto por respuesta genérica para no revelar qué correos tienen cuenta; el comportamiento actual sigue el requisito explícito del proyecto académico.

### Actualizar perfil

`PUT /api/usuarios/me`:
```json
{
  "nombre": "Nombre actualizado",
  "apellido": "Apellido",
  "telefono": "56912345678",
  "giro": "Rubro",
  "contactoNombre": "Contacto",
  "contactoCargo": "Cargo"
}
```
No se permite cambiar correo, RUT ni rol desde el perfil. Eso evita que una cuenta se autoeleve a ADMIN o cambie su identificador.

### Cambiar contraseña desde perfil

`PUT /api/usuarios/me/contrasena`:
```json
{ "actual": "Abc1234@", "nueva": "NuevaClave9@" }
```
El backend comprueba la contraseña actual y valida la política nueva.

## Política de contraseñas

- Entre 8 y 72 caracteres.
- Al menos una mayúscula Unicode.
- Al menos una minúscula Unicode.
- Al menos un dígito.
- Al menos un símbolo permitido.
- Sin espacios.
- Se acepta `ñ` como minúscula y `Ñ` como mayúscula; no se confunde una letra con un símbolo.
- BCrypt (factor 12); nunca se guarda la contraseña original.

Los tokens de activación y recuperación se generan aleatoriamente, expiran, se almacenan como SHA-256 y solo pueden consumirse una vez.

## Correo real

En `.env`, configura `MAIL_ENABLED=true`, host/puerto/usuario/contraseña SMTP y Para Gmail, se requiere contraseña de aplicación y configuración SMTP permitida por la cuenta. No guardes contraseñas reales en el repositorio.

En producción cambia `EXPOSE_DEV_TOKENS=false`, usa HTTPS, una clave JWT secreta fuerte, SMTP real y una política de CORS limitada a los dominios autorizados. No publiques la consola de desarrollo.

## Notas de integración Android

- Emulador Android: URL base del Gateway `http://10.0.2.2:8090/`.
- Teléfono físico: `http://IP_LOCAL_DEL_PC:8090/` (PC y teléfono en la misma Wi-Fi).
- No uses `localhost` desde el emulador/teléfono para apuntar al PC.
- La app debe enviar el JWT como `Authorization: Bearer ...`.
- El `DBHelper` se conserva para instalaciones, equipos, protocolos, mediciones e historiales. Usuarios/login/perfil dejan de consultarlo.

## Archivo de pruebas HTTP\n\n`pruebas-api.http` incluye solicitudes listas para IntelliJ HTTP Client o la extensión REST Client de VS Code. Reemplaza los valores `PEGA_AQUI...` por los tokens que devuelve el login o la creación de cuenta.\n\n## Diagnóstico

- `docker compose logs -f usuarios`
- `docker compose logs -f api-gateway`
- Si MySQL no conecta: revisa que la base y credenciales coincidan.
- Si no aparece en Eureka: revisa `EUREKA_URL`, el contenedor Eureka y la red Docker.
- Si el gateway responde 503: espera el registro de Usuarios en Eureka y vuelve a intentar.


## Ejecución alternativa sin Docker

El backend puede ejecutarse localmente mediante Spring Boot y Maven, sin utilizar contenedores Docker.

Esta modalidad requiere instalar y configurar previamente:

- Java JDK 21.
- Maven o Maven Wrapper (`mvnw.cmd`).
- MySQL 8.0 o compatible.
- Un cliente para administrar MySQL, como Laragon o MySQL Workbench.

### 1. Iniciar MySQL

Iniciar el servicio MySQL local y comprobar que esté disponible en el puerto `3306`.

Crear o verificar la existencia de la base de datos `induztek_usuarios`, utilizando la configuración de conexión definida para el microservicio Usuarios.

Asegurarse de que las credenciales coincidan con las propiedades de conexión de la configuración local.

### 2. Iniciar Eureka Server

Abrir una terminal en la carpeta `eureka-server` y ejecutar:

```cmd
mvnw.cmd spring-boot:run
```

Esperar a que Spring Boot termine de inicializarse.

Panel de Eureka:

http://localhost:8761

### 3. Iniciar el microservicio Usuarios

Abrir una segunda terminal en la carpeta `usuarios` y ejecutar:

```cmd
mvnw.cmd spring-boot:run
```

El servicio debe conectarse a MySQL y registrarse en Eureka.

Swagger:

http://localhost:8081/swagger-ui/index.html

### 4. Iniciar API Gateway

Abrir una tercera terminal en la carpeta `api-gateway` y ejecutar:

```cmd
mvnw.cmd spring-boot:run
```

El Gateway debe registrarse en Eureka y descubrir el microservicio Usuarios.

Swagger:

http://localhost:8090/swagger-ui/index.html

### 5. Comprobar el funcionamiento

Una vez iniciados los tres servicios, comprobar:

- Eureka disponible en el puerto `8761`.
- Usuarios disponible en el puerto `8081`.
- API Gateway disponible en el puerto `8090`.
- Comunicación entre Gateway y Usuarios.
- Conexión de Usuarios con MySQL.

Las solicitudes externas deben realizarse a través de API Gateway, utilizando:

`http://localhost:8090`

### Consideraciones

- Cada microservicio debe ejecutarse en una terminal independiente.
- Eureka debe iniciarse antes de Usuarios y API Gateway.
- MySQL debe estar disponible antes de iniciar Usuarios.
- La configuración local debe utilizar direcciones `localhost`, mientras que Docker utiliza los nombres internos de sus servicios.
- Las credenciales y secretos deben configurarse mediante variables de entorno o archivos locales no versionados.
- Esta modalidad no requiere Docker Desktop ni imágenes Docker previamente construidas.