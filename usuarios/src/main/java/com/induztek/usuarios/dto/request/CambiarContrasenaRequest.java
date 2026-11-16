package com.induztek.usuarios.dto.request;
import jakarta.validation.constraints.NotBlank;
public record CambiarContrasenaRequest(@NotBlank String actual, @NotBlank String nueva) { }
