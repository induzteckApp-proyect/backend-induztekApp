package com.induztek.usuarios.dto.request;
import jakarta.validation.constraints.*;
public record ActualizarPerfilRequest(
    @NotBlank @Size(min=2,max=100) @Pattern(regexp="^[\\p{L}][\\p{L} .'-]*$") String nombre,
    @Size(max=100) @Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]*$") String apellido,
    @NotBlank @Pattern(regexp="^[0-9]{8,12}$") String telefono,
    @Size(max=150) @Pattern(regexp="^$|^[\\p{L}\\p{N}][\\p{L}\\p{N} .,&()/'-]*$") String giro,
    @Size(max=120) @Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]*$") String contactoNombre,
    @Size(max=120) @Pattern(regexp="^$|^[\\p{L}][\\p{L} .'-]*$") String contactoCargo
) { }
