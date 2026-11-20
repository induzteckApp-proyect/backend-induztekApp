package com.induztek.usuarios.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(
    @NotBlank @Pattern(regexp="^[0-9.]{7,10}-[0-9Kk]$") String rut,
    @NotBlank @Size(min=2,max=100) @Pattern(regexp="^[\\p{L}][\\p{L} .'-]*$") String nombre,
    @Size(max=100) @Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]{1,99}$") String apellido,
    @NotBlank @Email @Size(max=160) String correo,
    @NotBlank @Pattern(regexp="^[0-9]{8,12}$") String telefono,
    @NotBlank @Pattern(regexp="^(ADMIN|CLIENTE|TECNICO_INGENIERO)$") String rol,
    @Size(max=150) @Pattern(regexp="^$|^[\\p{L}][\\p{L} ]{1,149}$") String giro,
    @Size(max=120) @Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]{1,119}$") String contactoNombre,
    @Size(max=120) @Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]{1,119}$") String contactoCargo
) {
}
