package com.induztek.usuarios.dto.request;
import jakarta.validation.constraints.*;
public record CrearUsuarioRequest(
    @NotBlank @Pattern(regexp="^[0-9.]{7,10}-[0-9Kk]$") String rut,
    @NotBlank @Size(min=2,max=100) @Pattern(regexp="^[\\p{L}][\\p{L} .'-]*$") String nombre,
    @Size(max=100) @Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]*$") String apellido,
    @NotBlank @Email @Size(max=160) String correo,
    @NotBlank @Pattern(regexp="^[0-9]{8,12}$") String telefono,
    @NotBlank @Pattern(regexp="^(ADMIN|CLIENTE|TECNICO_INGENIERO)$") String rol,
    @Size(max=150) @Pattern(regexp="^$|^[\\p{L}\\p{N}][\\p{L}\\p{N} .,&()/'-]*$") String giro,
    @Size(max=120) @Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]*$") String contactoNombre,
    @Size(max=120) @Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]*$") String contactoCargo
) { }
