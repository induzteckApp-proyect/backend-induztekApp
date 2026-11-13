package com.induztek.usuarios.controller;
import com.induztek.usuarios.dto.ApiDtos.*;
import com.induztek.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UsuarioService service; public AuthController(UsuarioService service){this.service=service;}
 @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest r){return service.login(r);}
 @PostMapping("/activar") public ResponseEntity<UsuarioResponse> activar(@Valid @RequestBody ActivarRequest r){return ResponseEntity.ok(service.activar(r));}
 @PostMapping("/recuperar") public MensajeResponse recuperar(@Valid @RequestBody RecuperarRequest r){return service.solicitarRecuperacion(r);}
 @PostMapping("/restablecer") public ResponseEntity<MensajeResponse> restablecer(@Valid @RequestBody RestablecerRequest r){service.restablecer(r);return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada correctamente."));}
}
