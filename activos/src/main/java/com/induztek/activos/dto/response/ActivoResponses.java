package com.induztek.activos.dto.response;
import java.math.BigDecimal;
public final class ActivoResponses {
    private ActivoResponses() { }
    public record RegionResponse(Long id, String nombre) { }
    public record ComunaResponse(Long id, String nombre, Long idRegion, String region) { }
    public record TipoEquipoResponse(Long id, String nombre, String clasificacion) { }
    public record InstalacionResponse(Long id, Long idUsuarioCliente, String nombre, String ubicacion,
                                      Long idComuna, String comuna, Long idRegion, String region,
                                      String observacionesAcceso) { }
    public record InstalacionConEquiposResponse(InstalacionResponse instalacion, java.util.List<EquipoResponse> equipos) { }
    public record ClienteActivosResponse(Long idUsuario, String rut, String nombre, String correo,
                                         java.util.List<InstalacionConEquiposResponse> instalaciones) { }
    public record EquipoResponse(Long id, Long idInstalacion, String tag, Long idTipoEquipo,
                                 String tipoEquipo, String clasificacion, String marca, String modelo,
                                 String numeroSerie, BigDecimal potenciaKva, BigDecimal tensiones,
                                 BigDecimal corriente, BigDecimal maxContactosUohm) { }
    public record EquipoInternoResponse(Long idEquipo, Long idInstalacion, Long idUsuarioCliente,
                                        Long idTipoEquipo, String tipoEquipo, String tag,
                                        String marca, String modelo, String numeroSerie, String nombreInstalacion, String ubicacion, String comuna, String region) { }
}
