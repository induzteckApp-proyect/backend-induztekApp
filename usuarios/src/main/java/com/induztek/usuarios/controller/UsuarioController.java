package com.induztek.usuarios.controller;

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
@RequestMapping("/api/usuarios")
@SecurityRequirement(name="bearerAuth")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_AGREGAR_PERSONAL')")
    @Operation(summary="Crear cuenta", description="Solo ADMIN puede crear cuentas. La cuenta queda pendiente de activación.")
    public ResponseEntity<MensajeResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    public UsuarioResponse miPerfil(Principal principal) { return service.miPerfil(principal.getName()); }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','TECNICO_INGENIERO')")
    public UsuarioResponse actualizar(@Valid @RequestBody ActualizarPerfilRequest request, Principal principal) {
        return service.actualizarPerfil(principal.getName(), request);
    }

    @PutMapping("/me/contrasena")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','TECNICO_INGENIERO')")
    public ResponseEntity<MensajeResponse> cambiar(@Valid @RequestBody CambiarContrasenaRequest request, Principal principal) {
        service.cambiarContrasena(principal.getName(), request);
        return ResponseEntity.ok(new MensajeResponse("Contraseña cambiada correctamente."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_LEER')")
    public UsuarioResponse obtener(@PathVariable Long id, Principal principal) {
        return service.obtener(id, principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_LEER')")
    public List<UsuarioResponse> listar() { return service.listar(); }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_EDICION')")
    @Operation(summary="Activar o desactivar cuenta", description="La activación de una cuenta pendiente debe realizarse mediante el token de invitación.")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoUsuarioRequest request) {
        return service.cambiarEstado(id, request.activo());
    }
}
