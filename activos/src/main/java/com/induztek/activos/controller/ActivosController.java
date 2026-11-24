package com.induztek.activos.controller;



import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.induztek.activos.dto.request.EquipoRequest;
import com.induztek.activos.dto.request.InstalacionRequest;
import com.induztek.activos.dto.response.ActivoResponses.*;
import com.induztek.activos.service.ActivosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Tag(name = "Activos", description = "Consulta y administración de clientes, instalaciones y equipos eléctricos.")
@RequestMapping("/api/activos")
@SecurityRequirement(name = "bearerAuth")
public class ActivosController {
    private final ActivosService service;
    private final String internalToken;

    public ActivosController(ActivosService service, @Value("${app.internal.token}") String internalToken) {
        this.service = service;
        this.internalToken = internalToken;
    }

        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/regiones")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    @Operation(summary = "Listar regiones", description = "Devuelve el catálogo de regiones utilizado en las instalaciones.")
    public List<RegionResponse> regiones() { return service.listarRegiones(); }

        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/comunas")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    @Operation(summary = "Listar comunas", description = "Incluye la región a la que pertenece cada comuna.")
    public List<ComunaResponse> comunas() { return service.listarComunas(); }

        @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/tipos-equipo")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    @Operation(summary = "Listar tipos de equipo", description = "Catálogo maestro propiedad exclusiva del microservicio Activos.")
    public List<TipoEquipoResponse> tiposEquipo() { return service.listarTiposEquipo(); }

        @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/instalaciones")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_CREACION')")
    @Operation(summary = "Crear instalación", description = "Registra una instalación para un cliente activo. Solo TECNICO_INGENIERO.")
    public ResponseEntity<InstalacionResponse> crearInstalacion(@Valid @RequestBody InstalacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearInstalacion(request));
    }

        @Operation(summary = "Listar clientes", description = "Busca clientes y resume sus activos asociados. El parámetro filtro permite limitar los resultados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/clientes")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_LEER')")
    public List<ClienteActivosResponse> clientes(@RequestParam(required = false) String filtro) {
        return service.listarClientesConActivos(filtro);
    }

        @Operation(summary = "Listar instalaciones de un cliente", description = "Obtiene las instalaciones asociadas al cliente indicado. Un usuario cliente solo puede consultar sus propios recursos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/clientes/{idCliente}/instalaciones")
    @PreAuthorize("hasAnyRole('TECNICO_INGENIERO','CLIENTE') and hasAuthority('PERM_LEER')")
    public List<InstalacionResponse> instalacionesCliente(@PathVariable Long idCliente, Authentication authentication) {
        validarPropiedadCliente(authentication, idCliente);
        return service.listarInstalacionesCliente(idCliente);
    }

        @Operation(summary = "Consultar instalación", description = "Obtiene el detalle de una instalación y valida el acceso del cliente propietario.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/instalaciones/{id}")
    @PreAuthorize("hasAnyRole('TECNICO_INGENIERO','CLIENTE') and hasAuthority('PERM_LEER')")
    public InstalacionResponse instalacion(@PathVariable Long id, Authentication authentication) {
        InstalacionResponse response = service.obtenerInstalacion(id);
        validarPropiedadCliente(authentication, response.idUsuarioCliente());
        return response;
    }

        @Operation(summary = "Crear equipo", description = "Registra un equipo eléctrico dentro de una instalación existente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping("/equipos")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_CREACION')")
    public ResponseEntity<EquipoResponse> crearEquipo(@Valid @RequestBody EquipoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearEquipo(request));
    }

        @Operation(summary = "Listar equipos de una instalación", description = "Obtiene los equipos asociados a la instalación indicada.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/instalaciones/{idInstalacion}/equipos")
    @PreAuthorize("hasAnyRole('TECNICO_INGENIERO','CLIENTE') and hasAuthority('PERM_LEER')")
    public List<EquipoResponse> equipos(@PathVariable Long idInstalacion, Authentication authentication) {
        InstalacionResponse instalacion = service.obtenerInstalacion(idInstalacion);
        validarPropiedadCliente(authentication, instalacion.idUsuarioCliente());
        return service.listarEquiposInstalacion(idInstalacion);
    }

        @Operation(summary = "Consultar equipo", description = "Obtiene el detalle de un equipo y valida el acceso a su instalación.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/equipos/{id}")
    @PreAuthorize("hasAnyRole('TECNICO_INGENIERO','CLIENTE') and hasAuthority('PERM_LEER')")
    public EquipoResponse equipo(@PathVariable Long id, Authentication authentication) {
        EquipoResponse response = service.obtenerEquipo(id);
        InstalacionResponse instalacion = service.obtenerInstalacion(response.idInstalacion());
        validarPropiedadCliente(authentication, instalacion.idUsuarioCliente());
        return response;
    }

        @Operation(summary = "Consultar tipo de equipo interno", description = "Endpoint de comunicación entre microservicios protegido mediante X-Internal-Token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/internal/tipos-equipo/{id}")
    public TipoEquipoResponse tipoEquipoInterno(@PathVariable Long id, @RequestHeader("X-Internal-Token") String token) {
        validarTokenInterno(token);
        return service.obtenerTipoEquipoInterno(id);
    }

        @Operation(summary = "Listar tipos de equipo internos", description = "Entrega el catálogo de tipos de equipo a otros microservicios mediante autenticación interna.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/internal/tipos-equipo")
    public List<TipoEquipoResponse> tiposEquipoInternos(@RequestHeader("X-Internal-Token") String token) {
        validarTokenInterno(token);
        return service.listarTiposEquipoInternos();
    }

        @Operation(summary = "Buscar equipos internos", description = "Busca equipos por tag y, opcionalmente, por instalación para uso entre microservicios.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/internal/equipos")
    public List<EquipoInternoResponse> buscarEquiposInternos(@RequestParam String tag,
            @RequestParam(required = false) String instalacion,
            @RequestHeader("X-Internal-Token") String token) {
        validarTokenInterno(token);
        return service.buscarEquiposInternos(tag, instalacion);
    }

        @Operation(summary = "Consultar equipo interno", description = "Entrega los datos internos de un equipo a otros microservicios mediante X-Internal-Token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@GetMapping("/internal/equipos/{id}")
    public EquipoInternoResponse equipoInterno(@PathVariable Long id, @RequestHeader("X-Internal-Token") String token) {
        validarTokenInterno(token);
        return service.obtenerEquipoInterno(id);
    }

    private void validarPropiedadCliente(Authentication authentication, Long idCliente) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))) {
            Long usuarioId = (Long) ((UsernamePasswordAuthenticationToken) authentication).getDetails();
            if (!usuarioId.equals(idCliente)) {
                throw new org.springframework.security.access.AccessDeniedException("Solo puedes consultar tus propios recursos.");
            }
        }
    }

    private void validarTokenInterno(String token) {
        if (!internalToken.equals(token)) {
            throw new org.springframework.security.access.AccessDeniedException("Credencial interna inválida.");
        }
    }
}
