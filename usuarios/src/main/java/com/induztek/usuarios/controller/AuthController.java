package com.induztek.usuarios.controller;




import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import com.induztek.usuarios.dto.request.*;
import com.induztek.usuarios.dto.response.*;
import com.induztek.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Autenticación", description = "Inicio de sesión, activación de cuentas y recuperación de contraseña.")
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioService service;
    public AuthController(UsuarioService service) { this.service = service; }

        @Operation(summary = "Iniciar sesión", description = "Valida las credenciales del usuario y devuelve el token de autenticación y los datos de sesión.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }

        @Operation(summary = "Activar cuenta", description = "Activa una cuenta mediante los datos y el código de activación proporcionados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/activar")
    public ResponseEntity<UsuarioResponse> activar(@Valid @RequestBody ActivarRequest request) {
        return ResponseEntity.ok(service.activar(request));
    }

        @Operation(summary = "Solicitar recuperación de contraseña", description = "Inicia el flujo de recuperación de contraseña y envía las instrucciones correspondientes.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/recuperar")
    public MensajeResponse recuperar(@Valid @RequestBody RecuperarRequest request) {
        return service.solicitarRecuperacion(request);
    }

        @Operation(summary = "Restablecer contraseña", description = "Valida el código de recuperación y establece una nueva contraseña.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/restablecer")
    public ResponseEntity<MensajeResponse> restablecer(@Valid @RequestBody RestablecerRequest request) {
        service.restablecer(request);
        return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada correctamente."));
    }
}
