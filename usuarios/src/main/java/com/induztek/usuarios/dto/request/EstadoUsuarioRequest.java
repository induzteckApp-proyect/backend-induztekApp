package com.induztek.usuarios.dto.request;
import jakarta.validation.constraints.NotNull;
public record EstadoUsuarioRequest(@NotNull Boolean activo) { }
