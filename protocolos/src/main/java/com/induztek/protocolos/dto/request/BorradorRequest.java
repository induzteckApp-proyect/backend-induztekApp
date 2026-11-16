package com.induztek.protocolos.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.List;
public record BorradorRequest(
    @NotNull Long idEquipo,
    @NotNull LocalDateTime fechaHoraPrueba,
    @NotNull BigDecimal temperaturaC,
    @NotNull BigDecimal humedadPct,
    @Size(max=2000) String observaciones,
    List<@NotNull Long> idsTiposPrueba
) { }
