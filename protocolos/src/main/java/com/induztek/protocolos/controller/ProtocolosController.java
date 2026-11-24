package com.induztek.protocolos.controller;



import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.induztek.protocolos.dto.request.*;
import com.induztek.protocolos.dto.response.ProtocoloResponses.*;
import com.induztek.protocolos.service.ProtocolosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Locale;

@RestController
@Tag(name = "Protocolos", description = "Catálogos, borradores, pruebas, estados e historial de protocolos técnicos.")
@RequestMapping("/api/protocolos")
@SecurityRequirement(name = "bearerAuth")
public class ProtocolosController {
    private final ProtocolosService service;
    public ProtocolosController(ProtocolosService service) { this.service = service; }

        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/catalogos/tipos-prueba")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    @Operation(summary = "Consultar tipos de prueba", description = "Permite consultar el catálogo completo o las pruebas compatibles con un tipo de equipo.")
    public List<TipoPruebaResponse> tiposPrueba(@RequestParam(required = false) Long idTipoEquipo) {
        return service.listarTiposPrueba(idTipoEquipo);
    }

        @Operation(summary = "Consultar parámetros de una prueba", description = "Obtiene los parámetros de medición asociados al tipo de prueba indicado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/catalogos/tipos-prueba/{idTipoPrueba}/parametros")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    public List<ParametroResponse> parametros(@PathVariable Long idTipoPrueba) {
        return service.listarParametros(idTipoPrueba);
    }

        @Operation(summary = "Registrar protocolo inmediatamente", description = "Crea un protocolo completo a partir de los datos enviados por un técnico autorizado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/registro-inmediato")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_CREACION')")
    public ResponseEntity<ProtocoloResponse> registrarInmediato(
            @Valid @RequestBody RegistrarProtocoloRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarProtocolo(request, userId(auth)));
    }

        @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/borradores")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_CREACION')")
    @Operation(summary = "Crear borrador", description = "Crea la cabecera del protocolo y sus pruebas seleccionadas.")
    public ResponseEntity<ProtocoloResponse> crearBorrador(@Valid @RequestBody BorradorRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearBorrador(request, userId(auth)));
    }

        @Operation(summary = "Listar mis borradores", description = "Obtiene los borradores pertenecientes al técnico autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/borradores/me")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_LEER')")
    public List<ProtocoloResponse> borradores(Authentication auth) {
        return service.listarBorradores(userId(auth));
    }

        @Operation(summary = "Actualizar cabecera de borrador", description = "Modifica los datos generales de un borrador propio.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PutMapping("/{id}/borrador")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_EDICION')")
    public ProtocoloResponse actualizarCabecera(@PathVariable Long id,
            @Valid @RequestBody BorradorRequest request, Authentication auth) {
        return service.actualizarCabeceraBorrador(id, request, userId(auth));
    }

        @Operation(summary = "Guardar pruebas seleccionadas", description = "Actualiza el conjunto de tipos de prueba asociados al borrador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/{id}/pruebas/seleccionadas")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_EDICION')")
    public ProtocoloResponse guardarPruebasSeleccionadas(@PathVariable Long id,
            @Valid @RequestBody GuardarPruebasSeleccionadasRequest request, Authentication auth) {
        return service.guardarPruebasSeleccionadas(id, request.idsTiposPrueba(), userId(auth));
    }

        @Operation(summary = "Guardar prueba de protocolo", description = "Agrega o actualiza los datos de una prueba dentro del protocolo indicado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@RequestMapping(path = "/{id}/pruebas", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_EDICION')")
    public ProtocoloResponse guardarPrueba(@PathVariable Long id,
            @Valid @RequestBody GuardarPruebaRequest request, Authentication auth) {
        return service.guardarPrueba(id, request, userId(auth));
    }

        @Operation(summary = "Cambiar estado de usuario", description = "Activa o desactiva una cuenta según los permisos del administrador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAnyAuthority('PERM_FORMALIZACION','PERM_ELIMINACION','PERM_EDICION')")
    public ProtocoloResponse cambiarEstado(@PathVariable Long id,
            @Valid @RequestBody CambioEstadoRequest request, Authentication auth) {
        String estado = request.estado().trim().toUpperCase(Locale.ROOT);
        boolean permitido = switch (estado) {
            case "FORMALIZADO" -> tienePermiso(auth, "PERM_FORMALIZACION");
            case "ANULADO" -> tienePermiso(auth, "PERM_ELIMINACION");
            default -> tienePermiso(auth, "PERM_EDICION");
        };
        if (!permitido) {
            throw new org.springframework.security.access.AccessDeniedException("No tienes el permiso requerido para este cambio de estado.");
        }
        return service.cambiarEstado(id, request, userId(auth));
    }

        @Operation(summary = "Preparar protocolo para edición", description = "Prepara un protocolo existente para iniciar su edición según las reglas del dominio.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/{id}/preparar-edicion")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_EDICION')")
    public ProtocoloResponse prepararEdicion(@PathVariable Long id, Authentication auth) {
        return service.prepararEdicion(id, userId(auth));
    }

        @Operation(summary = "Reemplazar protocolo", description = "Crea un protocolo de reemplazo vinculado al protocolo original.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/{id}/reemplazar")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_CREACION') and hasAuthority('PERM_ELIMINACION')")
    public ResponseEntity<ProtocoloResponse> reemplazar(@PathVariable Long id,
            @Valid @RequestBody BorradorRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reemplazar(id, request, userId(auth)));
    }

        @Operation(summary = "Consultar historial de protocolos", description = "Obtiene el historial de protocolos visible para el técnico autenticado, incluyendo datos de equipo, cliente y técnico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/historial")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_LEER')")
    public List<ProtocoloResponse> historialTecnico(Authentication auth) {
        return service.listarHistorialTecnico(userId(auth));
    }

        @Operation(summary = "Consultar protocolos de un equipo", description = "Obtiene los protocolos asociados al equipo indicado, respetando el rol y alcance del usuario.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/equipos/{idEquipo}")
    @PreAuthorize("hasAnyRole('TECNICO_INGENIERO','CLIENTE') and hasAuthority('PERM_LEER')")
    public List<ProtocoloResponse> protocolosEquipo(@PathVariable Long idEquipo, Authentication auth) {
        return service.listarPorEquipo(idEquipo, userId(auth), esTecnico(auth));
    }

        @Operation(summary = "Consultar mis protocolos", description = "Obtiene los protocolos asociados al cliente autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/clientes/me")
    @PreAuthorize("hasRole('CLIENTE') and hasAuthority('PERM_LEER')")
    public List<ProtocoloResponse> protocolosCliente(Authentication auth) {
        return service.listarProtocolosCliente(userId(auth));
    }

        @Operation(summary = "Consultar detalle de protocolo", description = "Obtiene el detalle de un protocolo y valida que el usuario tenga acceso al recurso.")
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
    @PreAuthorize("hasAnyRole('TECNICO_INGENIERO','CLIENTE') and hasAuthority('PERM_LEER')")
    public ProtocoloResponse detalle(@PathVariable Long id, Authentication auth) {
        return service.obtenerDetalle(id, userId(auth), esTecnico(auth));
    }

    private Long userId(Authentication authentication) {
        return (Long) ((UsernamePasswordAuthenticationToken) authentication).getDetails();
    }
    private boolean tienePermiso(Authentication auth, String permiso) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(permiso));
    }
    private boolean esTecnico(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECNICO_INGENIERO"));
    }
}
