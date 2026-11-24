package com.induztek.usuarios.controller;



import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.induztek.usuarios.dto.request.*;
import com.induztek.usuarios.dto.response.*;
import com.induztek.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@Tag(name = "Usuarios", description = "Administración de cuentas y gestión del perfil del usuario autenticado.")
@RequestMapping("/api/usuarios")
@SecurityRequirement(name="bearerAuth")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }

        @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_AGREGAR_PERSONAL')")
    @Operation(summary="Crear cuenta", description="Solo ADMIN puede crear cuentas. La cuenta queda pendiente de activación.")
    public ResponseEntity<MensajeResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

        @Operation(summary = "Consultar mi perfil", description = "Devuelve los datos del perfil correspondiente al usuario autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/me")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    public UsuarioResponse miPerfil(Principal principal) { return service.miPerfil(principal.getName()); }

        @Operation(summary = "Actualizar mi perfil", description = "Actualiza los datos editables del perfil del usuario autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PutMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','TECNICO_INGENIERO')")
    public UsuarioResponse actualizar(@Valid @RequestBody ActualizarPerfilRequest request, Principal principal) {
        return service.actualizarPerfil(principal.getName(), request);
    }

        @Operation(summary = "Cambiar mi contraseña", description = "Valida la contraseña actual y actualiza la credencial del usuario autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PutMapping("/me/contrasena")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','TECNICO_INGENIERO')")
    public ResponseEntity<MensajeResponse> cambiar(@Valid @RequestBody CambiarContrasenaRequest request, Principal principal) {
        service.cambiarContrasena(principal.getName(), request);
        return ResponseEntity.ok(new MensajeResponse("Contraseña cambiada correctamente."));
    }

        @Operation(summary = "Consultar usuario", description = "Obtiene los datos de un usuario según el identificador indicado y las reglas de autorización.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    public UsuarioResponse obtener(@PathVariable Long id, Principal principal) {
        return service.obtener(id, principal.getName());
    }

        @Operation(summary = "Listar usuarios", description = "Obtiene el listado de usuarios. Operación restringida a administradores.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_LEER')")
    public List<UsuarioResponse> listar() { return service.listar(); }

        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_EDICION')")
    @Operation(summary="Activar o desactivar cuenta", description="La activación de una cuenta pendiente debe realizarse mediante el token de invitación.")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoUsuarioRequest request) {
        return service.cambiarEstado(id, request.activo());
    }
}
