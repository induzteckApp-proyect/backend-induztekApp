package com.induztek.protocolos.dto.request;
import jakarta.validation.constraints.NotNull; import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
public record MedicionRequest(@NotNull Long idParametro, @NotNull @Digits(integer=10, fraction=4) BigDecimal valor) { }
