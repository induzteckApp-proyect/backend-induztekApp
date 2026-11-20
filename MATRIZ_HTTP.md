# Matriz HTTP de Induztek

La aplicación Android consume el API Gateway en `http://localhost:8090/` desde el computador. En el emulador Android se utiliza `http://10.0.2.2:8090/`. El Gateway enruta `/api/activos/**` a Activos, `/api/protocolos/**` a Protocolos y el resto de `/api/**` a Usuarios; `/media/avatars/**` también se dirige a Usuarios.

## Usuarios

| Método | Ruta | Éxito | Acceso |
|---|---|---:|---|
| POST | `/api/auth/login` | 200 | Público |
| POST | `/api/auth/activar` | 200 | Público; código de invitación |
| POST | `/api/auth/recuperar` | 200 | Público; envía código por correo |
| POST | `/api/auth/restablecer` | 200 | Público; código de recuperación |
| POST | `/api/usuarios` | 201 | ADMIN |
| GET | `/api/usuarios/me` | 200 | Usuario autenticado |
| PUT | `/api/usuarios/me` | 200 | ADMIN, CLIENTE, TECNICO_INGENIERO |
| PUT | `/api/usuarios/me/contrasena` | 200 | ADMIN, CLIENTE, TECNICO_INGENIERO |
| POST | `/api/usuarios/me/avatar` | 200 | ADMIN, CLIENTE, TECNICO_INGENIERO |
| GET | `/api/usuarios` | 200 | ADMIN |
| GET | `/api/usuarios/{id}` | 200 | Usuario con permiso de lectura |
| PATCH | `/api/usuarios/{id}/estado` | 200 | ADMIN |
| GET | `/api/internal/usuarios` | 200 | Servicio interno con `X-Internal-Token` |
| GET | `/api/internal/usuarios/{id}` | 200 | Servicio interno con `X-Internal-Token` |

## Activos

| Método | Ruta | Éxito | Acceso |
|---|---|---:|---|
| GET | `/api/activos/regiones` | 200 | Usuario con permiso de lectura |
| GET | `/api/activos/comunas` | 200 | Usuario con permiso de lectura |
| GET | `/api/activos/tipos-equipo` | 200 | Usuario con permiso de lectura |
| GET | `/api/activos/clientes?filtro=...` | 200 | TECNICO_INGENIERO |
| GET | `/api/activos/clientes/{idCliente}/instalaciones` | 200 | Técnico o el cliente propietario |
| POST | `/api/activos/instalaciones` | 201 | TECNICO_INGENIERO |
| GET | `/api/activos/instalaciones/{id}` | 200 | Técnico o el cliente propietario |
| GET | `/api/activos/instalaciones/{idInstalacion}/equipos` | 200 | Técnico o el cliente propietario |
| POST | `/api/activos/equipos` | 201 | TECNICO_INGENIERO |
| GET | `/api/activos/equipos/{id}` | 200 | Técnico o el cliente propietario |
| GET | `/api/activos/internal/tipos-equipo` | 200 | Servicio interno con `X-Internal-Token` |
| GET | `/api/activos/internal/tipos-equipo/{id}` | 200 | Servicio interno con `X-Internal-Token` |
| GET | `/api/activos/internal/equipos?tag=...` | 200 | Servicio interno con `X-Internal-Token` |
| GET | `/api/activos/internal/equipos/{id}` | 200 | Servicio interno con `X-Internal-Token` |

## Protocolos

| Método | Ruta | Éxito | Acceso |
|---|---|---:|---|
| GET | `/api/protocolos/catalogos/tipos-prueba?idTipoEquipo=...` | 200 | Usuario con permiso de lectura |
| GET | `/api/protocolos/catalogos/tipos-prueba/{idTipoPrueba}/parametros` | 200 | Usuario con permiso de lectura |
| POST | `/api/protocolos/registro-inmediato` | 201 | TECNICO_INGENIERO |
| POST | `/api/protocolos/borradores` | 201 | TECNICO_INGENIERO |
| GET | `/api/protocolos/borradores/me` | 200 | TECNICO_INGENIERO |
| PUT | `/api/protocolos/{id}/borrador` | 200 | TECNICO_INGENIERO |
| POST | `/api/protocolos/{id}/pruebas/seleccionadas` | 200 | TECNICO_INGENIERO |
| POST o PUT | `/api/protocolos/{id}/pruebas` | 200 | TECNICO_INGENIERO |
| PUT | `/api/protocolos/{id}/estado` | 200 | TECNICO_INGENIERO con permiso del cambio |
| POST | `/api/protocolos/{id}/preparar-edicion` | 200 | TECNICO_INGENIERO |
| POST | `/api/protocolos/{id}/reemplazar` | 201 | TECNICO_INGENIERO |
| GET | `/api/protocolos/historial` | 200 | TECNICO_INGENIERO |
| GET | `/api/protocolos/equipos/{idEquipo}` | 200 | Técnico o cliente propietario |
| GET | `/api/protocolos/clientes/me` | 200 | CLIENTE |
| GET | `/api/protocolos/{id}` | 200 | Técnico o cliente autorizado |

## Errores transversales

| Código | Uso |
|---:|---|
| 400 | Validación Jakarta, campos inválidos, parámetros inválidos o JSON mal formado |
| 401 | JWT ausente, inválido o expirado |
| 403 | Rol/permisos insuficientes, acceso a recursos de otro cliente o token interno inválido |
| 404 | Recurso no encontrado |
| 409 | Duplicados, restricciones de integridad o transición de estado no permitida |
| 500 | Error interno inesperado |
| 503 | No se pudo enviar el correo de recuperación |

Los controladores que crean recursos devuelven `201 Created`. Las consultas y operaciones que actualizan un recurso existente devuelven `200 OK`. Actualmente no hay una operación que utilice `204 No Content`, porque los endpoints de escritura devuelven un DTO o un mensaje.

Los tres manejadores globales traducen validaciones y excepciones a respuestas HTTP. Los errores contienen el campo `message`, que Android utiliza para presentar el motivo al usuario.
