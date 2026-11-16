package com.induztek.protocolos.dto.request;
import jakarta.validation.constraints.NotNull;
import java.util.List;
public record GuardarPruebasSeleccionadasRequest(List<@NotNull Long> idsTiposPrueba) { }
