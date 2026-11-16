package com.induztek.protocolos.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record RegistrarProtocoloRequest(
    @NotNull Long idEquipo,
    @NotNull Long idTipoPrueba,
    @NotNull LocalDateTime fechaHoraPrueba,
    @NotNull BigDecimal temperaturaC,
    @NotNull BigDecimal humedadPct,
    @Size(max=120) String elemento,
    @NotEmpty Map<@NotNull String, @NotNull BigDecimal> mediciones
) { }
