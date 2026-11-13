package com.induztek.usuarios.dto;

import com.induztek.usuarios.model.RolUsuario;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class ApiDtos {

    private ApiDtos() {}

    public record LoginRequest(
            @NotBlank @Email String correo,
            @NotBlank String contrasena
    ) {}

    public record CrearUsuarioRequest(
            @NotBlank @Pattern(regexp = "^[0-9.]{7,10}-[0-9Kk]$") String rut,
            @NotBlank @Size(min = 2, max = 100) @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]*$") String nombre,
            @Size(max = 100) @Pattern(regexp = "^$|^[\\p{L}][\\p{L} .'-]*$") String apellido,
            @NotBlank @Email @Size(max = 160) String correo,
            @NotBlank @Pattern(regexp = "^[0-9]{8,12}$") String telefono,
            @NotNull RolUsuario rol,
            @Size(max = 150) @Pattern(regexp = "^$|^[\\p{L}\\p{N}][\\p{L}\\p{N} .,&()/'-]*$") String giro,
            @Size(max = 120) @Pattern(regexp = "^$|^[\\p{L}][\\p{L} .'-]*$") String contactoNombre,
            @Size(max = 120) @Pattern(regexp = "^$|^[\\p{L}][\\p{L} .'-]*$") String contactoCargo
    ) {}

    public record ActualizarPerfilRequest(
            @NotBlank @Size(min = 2, max = 100) @Pattern(regexp = "^[\\p{L}][\\p{L} .'-]*$") String nombre,
            @Size(max = 100) @Pattern(regexp = "^$|^[\\p{L}][\\p{L} .'-]*$") String apellido,
            @NotBlank @Pattern(regexp = "^[0-9]{8,12}$") String telefono,
            @Size(max = 150) @Pattern(regexp = "^$|^[\\p{L}\\p{N}][\\p{L}\\p{N} .,&()/'-]*$") String giro,
            @Size(max = 120) @Pattern(regexp = "^$|^[\\p{L}][\\p{L} .'-]*$") String contactoNombre,
            @Size(max = 120) @Pattern(regexp = "^$|^[\\p{L}][\\p{L} .'-]*$") String contactoCargo
    ) {}

    public record CambiarContrasenaRequest(
            @NotBlank String actual,
            @NotBlank String nueva
    ) {}

    public record RecuperarRequest(
            @NotBlank @Email String correo
    ) {}

    public record RestablecerRequest(
            @NotBlank String token,
            @NotBlank String nuevaContrasena
    ) {}

    public record ActivarRequest(
            @NotBlank String token,
            @NotBlank String nuevaContrasena
    ) {}

    public record AuthResponse(
            String token,
            String tipo,
            UsuarioResponse usuario
    ) {}

    public record UsuarioResponse(
            Long id,
            String rut,
            String nombre,
            String apellido,
            String correo,
            String telefono,
            RolUsuario rol,
            boolean activo,
            String giro,
            String contactoNombre,
            String contactoCargo,
            String avatarUrl,
            LocalDateTime creadoEn
    ) {}

    public record MensajeResponse(
            String mensaje,
            String tokenDesarrollo
    ) {
        public MensajeResponse(String mensaje) {
            this(mensaje, null);
        }
    }
}