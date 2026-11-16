package com.induztek.usuarios.controller;

import com.induztek.usuarios.dto.request.*;
import com.induztek.usuarios.dto.response.*;
import com.induztek.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioService service;
    public AuthController(UsuarioService service) { this.service = service; }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }

    @PostMapping("/activar")
    public ResponseEntity<UsuarioResponse> activar(@Valid @RequestBody ActivarRequest request) {
        return ResponseEntity.ok(service.activar(request));
    }

    @PostMapping("/recuperar")
    public MensajeResponse recuperar(@Valid @RequestBody RecuperarRequest request) {
        return service.solicitarRecuperacion(request);
    }

    @PostMapping("/restablecer")
    public ResponseEntity<MensajeResponse> restablecer(@Valid @RequestBody RestablecerRequest request) {
        service.restablecer(request);
        return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada correctamente."));
    }
}
