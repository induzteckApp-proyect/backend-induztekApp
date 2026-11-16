package com.induztek.protocolos.dto.response;
import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.List;
public final class ProtocoloResponses {
    private ProtocoloResponses() { }
    public record TipoPruebaResponse(Long id, String nombre) { }
    public record ParametroResponse(Long id, Long idTipoPrueba, String codigo, String nombre, String unidad) { }
    public record MedicionResponse(Long id, Long idParametro, String parametro, String unidad, BigDecimal valor) { }
    public record PruebaResponse(Long id, Long idTipoPrueba, String tipoPrueba, String elemento,
                                 String momento, boolean realizada, String motivoNoRealizada,
                                 Integer idResultado, String resultado, List<MedicionResponse> mediciones) { }
    public record ProtocoloResponse(Long id, String numero, Long idEquipo, Long idUsuarioRegistra,
                                    LocalDateTime fechaHoraPrueba, BigDecimal temperaturaC,
                                    BigDecimal humedadPct, String observaciones, String estadoCodigo,
                                    String estadoNombre, Long idProtocoloReemplazado,
                                    List<PruebaResponse> pruebas, String resultadoGlobal,
                                    Long idUsuarioCliente, String clienteNombre, String clienteCorreo,
                                    String instalacionNombre, String ubicacion, String comuna, String region,
                                    String tagEquipo, String tipoEquipo, String marca, String modelo,
                                    String numeroSerie, String tecnicoNombre, String tecnicoCorreo) { }
    public record MensajeResponse(String mensaje) { }
}
