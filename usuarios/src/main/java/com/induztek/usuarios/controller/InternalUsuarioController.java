package com.induztek.usuarios.controller;

import com.induztek.usuarios.dto.response.UsuarioInternoResponse;
import com.induztek.usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/internal/usuarios")
public class InternalUsuarioController {
    private final UsuarioService service;
    private final String tokenInterno;
    public InternalUsuarioController(UsuarioService service,
            @Value("${app.internal.token:induztek-internal-dev-token-change-me}") String tokenInterno) {
        this.service = service;
        this.tokenInterno = tokenInterno;
    }
    @GetMapping("/{id}")
    public UsuarioInternoResponse obtener(@PathVariable Long id, @RequestHeader("X-Internal-Token") String token) {
        validar(token);
        return service.obtenerInterno(id);
    }
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
