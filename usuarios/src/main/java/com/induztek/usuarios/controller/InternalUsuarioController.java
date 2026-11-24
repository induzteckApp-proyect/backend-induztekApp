package com.induztek.usuarios.controller;




import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import com.induztek.usuarios.dto.response.UsuarioInternoResponse;
import com.induztek.usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Tag(name = "Usuarios internos", description = "Endpoints internos para intercambio de información entre microservicios.")
@RequestMapping("/api/internal/usuarios")
public class InternalUsuarioController {
    private final UsuarioService service;
    private final String tokenInterno;
    public InternalUsuarioController(UsuarioService service,
            @Value("${app.internal.token:induztek-internal-dev-token-change-me}") String tokenInterno) {
        this.service = service;
        this.tokenInterno = tokenInterno;
    }
        @Operation(summary = "Consultar usuario para uso interno", description = "Entrega los datos básicos de un usuario a otro microservicio. Requiere la cabecera X-Internal-Token válida.")
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
    public UsuarioInternoResponse obtener(@PathVariable Long id, @RequestHeader("X-Internal-Token") String token) {
        validar(token);
        return service.obtenerInterno(id);
    }
        @Operation(summary = "Listar usuarios para uso interno", description = "Entrega usuarios filtrados opcionalmente por rol a otros microservicios. Requiere la cabecera X-Internal-Token válida.")
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
    public List<UsuarioInternoResponse> listar(@RequestParam(required=false) String rol,
            @RequestHeader("X-Internal-Token") String token) {
        validar(token);
        return service.listarInternos(rol);
    }
    private void validar(String recibido) {
        if (!tokenInterno.equals(recibido)) throw new AccessDeniedException("Credencial interna inválida.");
    }
}
