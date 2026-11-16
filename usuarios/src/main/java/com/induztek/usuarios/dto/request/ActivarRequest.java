package com.induztek.usuarios.dto.request;
import jakarta.validation.constraints.NotBlank;
public record ActivarRequest(@NotBlank String token, @NotBlank String nuevaContrasena) { }
