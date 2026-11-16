package com.induztek.activos.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
public record EquipoRequest(
    @NotNull Long idInstalacion,
    @NotBlank @Size(max = 60) String tag,
    @NotNull Long idTipoEquipo,
    @Size(max = 100) String marca,
    @Size(max = 100) String modelo,
    @Size(max = 100) String numeroSerie,
    @PositiveOrZero BigDecimal potenciaKva,
    @PositiveOrZero BigDecimal tensiones,
    @PositiveOrZero BigDecimal corriente,
    @PositiveOrZero BigDecimal maxContactosUohm
) { }
