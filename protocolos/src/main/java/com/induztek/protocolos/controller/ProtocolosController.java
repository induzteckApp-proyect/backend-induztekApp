package com.induztek.protocolos.controller;

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
@RequestMapping("/api/protocolos")
@SecurityRequirement(name = "bearerAuth")
public class ProtocolosController {
    private final ProtocolosService service;
    public ProtocolosController(ProtocolosService service) { this.service = service; }

    @GetMapping("/catalogos/tipos-prueba")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    @Operation(summary = "Consultar tipos de prueba", description = "Permite consultar el catálogo completo o las pruebas compatibles con un tipo de equipo.")
    public List<TipoPruebaResponse> tiposPrueba(@RequestParam(required = false) Long idTipoEquipo) {
        return service.listarTiposPrueba(idTipoEquipo);
    }

    @GetMapping("/catalogos/tipos-prueba/{idTipoPrueba}/parametros")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    public List<ParametroResponse> parametros(@PathVariable Long idTipoPrueba) {
        return service.listarParametros(idTipoPrueba);
    }

    @PostMapping("/registro-inmediato")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_CREACION')")
    public ResponseEntity<ProtocoloResponse> registrarInmediato(
            @Valid @RequestBody RegistrarProtocoloRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarProtocolo(request, userId(auth)));
    }

    @PostMapping("/borradores")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_CREACION')")
    @Operation(summary = "Crear borrador", description = "Crea la cabecera del protocolo y sus pruebas seleccionadas.")
    public ResponseEntity<ProtocoloResponse> crearBorrador(@Valid @RequestBody BorradorRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearBorrador(request, userId(auth)));
    }

    @GetMapping("/borradores/me")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_LEER')")
    public List<ProtocoloResponse> borradores(Authentication auth) {
        return service.listarBorradores(userId(auth));
    }

    @PutMapping("/{id}/borrador")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_EDICION')")
    public ProtocoloResponse actualizarCabecera(@PathVariable Long id,
            @Valid @RequestBody BorradorRequest request, Authentication auth) {
        return service.actualizarCabeceraBorrador(id, request, userId(auth));
    }

    @PostMapping("/{id}/pruebas/seleccionadas")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_EDICION')")
    public ProtocoloResponse guardarPruebasSeleccionadas(@PathVariable Long id,
            @Valid @RequestBody GuardarPruebasSeleccionadasRequest request, Authentication auth) {
        return service.guardarPruebasSeleccionadas(id, request.idsTiposPrueba(), userId(auth));
    }

    @RequestMapping(path = "/{id}/pruebas", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_EDICION')")
    public ProtocoloResponse guardarPrueba(@PathVariable Long id,
            @Valid @RequestBody GuardarPruebaRequest request, Authentication auth) {
        return service.guardarPrueba(id, request, userId(auth));
    }

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

    @PostMapping("/{id}/preparar-edicion")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_EDICION')")
    public ProtocoloResponse prepararEdicion(@PathVariable Long id, Authentication auth) {
        return service.prepararEdicion(id, userId(auth));
    }

    @PostMapping("/{id}/reemplazar")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_CREACION') and hasAuthority('PERM_ELIMINACION')")
    public ResponseEntity<ProtocoloResponse> reemplazar(@PathVariable Long id,
            @Valid @RequestBody BorradorRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reemplazar(id, request, userId(auth)));
    }

    @GetMapping("/historial")
    @PreAuthorize("hasRole('TECNICO_INGENIERO') and hasAuthority('PERM_LEER')")
    public List<ProtocoloResponse> historialTecnico(Authentication auth) {
        return service.listarHistorialTecnico(userId(auth));
    }

    @GetMapping("/equipos/{idEquipo}")
    @PreAuthorize("hasAnyRole('TECNICO_INGENIERO','CLIENTE') and hasAuthority('PERM_LEER')")
    public List<ProtocoloResponse> protocolosEquipo(@PathVariable Long idEquipo, Authentication auth) {
        return service.listarPorEquipo(idEquipo, userId(auth), esTecnico(auth));
    }

    @GetMapping("/clientes/me")
    @PreAuthorize("hasRole('CLIENTE') and hasAuthority('PERM_LEER')")
    public List<ProtocoloResponse> protocolosCliente(Authentication auth) {
        return service.listarProtocolosCliente(userId(auth));
    }

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
