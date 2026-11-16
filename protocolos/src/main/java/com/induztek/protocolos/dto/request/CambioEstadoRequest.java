package com.induztek.protocolos.dto.request;
import jakarta.validation.constraints.NotBlank;
public record CambioEstadoRequest(@NotBlank String estado, String motivo) { }
