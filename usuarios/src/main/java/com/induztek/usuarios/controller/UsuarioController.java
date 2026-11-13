package com.induztek.usuarios.controller;
import com.induztek.usuarios.dto.ApiDtos.*;
import com.induztek.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/usuarios") @SecurityRequirement(name="bearerAuth")
public class UsuarioController {
 private final UsuarioService service; public UsuarioController(UsuarioService service){this.service=service;}
 @PostMapping public ResponseEntity<MensajeResponse> crear(@Valid @RequestBody CrearUsuarioRequest r){
  return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(r));
 }
 @GetMapping("/me") public UsuarioResponse miPerfil(java.security.Principal p){return service.miPerfil(p.getName());}
 @PutMapping("/me") public UsuarioResponse actualizar(@Valid @RequestBody ActualizarPerfilRequest r,java.security.Principal p){return service.actualizarPerfil(p.getName(),r);}
 @PutMapping("/me/contrasena") public ResponseEntity<MensajeResponse> cambiar(@Valid @RequestBody CambiarContrasenaRequest r,java.security.Principal p){service.cambiarContrasena(p.getName(),r);return ResponseEntity.ok(new MensajeResponse("Contraseña cambiada correctamente."));}
 @GetMapping("/{id}") public UsuarioResponse obtener(@PathVariable Long id,java.security.Principal p){return service.obtener(id,p.getName());}
 @GetMapping @PreAuthorize("hasRole('ADMIN')") public List<UsuarioResponse> listar(){return service.listar();}
}
