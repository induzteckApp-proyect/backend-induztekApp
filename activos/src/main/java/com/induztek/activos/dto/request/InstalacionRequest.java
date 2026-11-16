package com.induztek.activos.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
public record InstalacionRequest(
    @NotNull Long idUsuarioCliente,
    @NotBlank @Size(max = 140) String nombre,
    @Size(max = 250) String ubicacion,
    @NotNull Long idComuna,
    String observacionesAcceso
) { }
