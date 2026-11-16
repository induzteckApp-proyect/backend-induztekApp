package com.induztek.protocolos.dto.request;
import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size; import java.util.List;
public record GuardarPruebaRequest(
    Long idPrueba,
    @NotNull Long idTipoPrueba,
    @Size(max=120) String elemento,
    @NotBlank String momento,
    boolean realizada,
    String motivoNoRealizada,
    Integer idResultado,
    @Valid List<MedicionRequest> mediciones
) { }
