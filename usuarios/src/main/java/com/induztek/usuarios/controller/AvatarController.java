package com.induztek.usuarios.controller;




import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Fotografía de perfil", description = "Carga y actualización de la imagen de perfil del usuario.")
@RequestMapping("/api/usuarios/me/avatar")
@SecurityRequirement(name="bearerAuth")
public class AvatarController {
    private final AvatarStorageService storage;
    private final UsuarioService usuarios;
    public AvatarController(AvatarStorageService storage, UsuarioService usuarios) {
        this.storage = storage;
        this.usuarios = usuarios;
    }
        @Operation(summary = "Actualizar fotografía de perfil", description = "Carga una imagen de avatar para el usuario autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operación completada correctamente."),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos de entrada no válidos."),
        @ApiResponse(responseCode = "401", description = "No autenticado o token inválido."),
        @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta operación."),
        @ApiResponse(responseCode = "404", description = "No se encontró el recurso solicitado."),
        @ApiResponse(responseCode = "409", description = "Conflicto con el estado actual del recurso."),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar la solicitud.")
    })
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','TECNICO_INGENIERO')")
    public UsuarioResponse subir(@RequestPart("archivo") MultipartFile archivo, Principal principal) throws java.io.IOException {
        return usuarios.actualizarAvatar(principal.getName(), storage.guardar(archivo));
    }
}
