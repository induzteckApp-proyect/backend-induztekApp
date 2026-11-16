package com.induztek.usuarios.controller;

import com.induztek.usuarios.dto.response.UsuarioResponse;
import com.induztek.usuarios.service.AvatarStorageService;
import com.induztek.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;

@RestController
@RequestMapping("/api/usuarios/me/avatar")
@SecurityRequirement(name="bearerAuth")
public class AvatarController {
    private final AvatarStorageService storage;
    private final UsuarioService usuarios;
    public AvatarController(AvatarStorageService storage, UsuarioService usuarios) {
        this.storage = storage;
        this.usuarios = usuarios;
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PERM_EDICION')")
    public UsuarioResponse subir(@RequestPart("archivo") MultipartFile archivo, Principal principal) throws java.io.IOException {
        return usuarios.actualizarAvatar(principal.getName(), storage.guardar(archivo));
    }
}
