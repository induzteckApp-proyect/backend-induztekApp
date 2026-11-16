package com.induztek.protocolos.service;

import com.induztek.protocolos.client.ActivosClient;
import com.induztek.protocolos.client.UsuariosClient;
import com.induztek.protocolos.dto.request.BorradorRequest;
import com.induztek.protocolos.dto.request.CambioEstadoRequest;
import com.induztek.protocolos.model.*;
import com.induztek.protocolos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProtocolosServiceTest {
    @Mock EstadoProtocoloRepository estados;
    @Mock TipoPruebaRepository tipos;
    @Mock TipoPruebaEquipoRepository compatibilidades;
    @Mock ParametroRepository parametros;
    @Mock ProtocoloRepository protocolos;
    @Mock PruebaRepository pruebas;
    @Mock MedicionRepository mediciones;
    @Mock ActivosClient activos;
    @Mock UsuariosClient usuarios;
    private ProtocolosService service;

    @BeforeEach
    void configurar() {
        service = new ProtocolosService(estados, tipos, compatibilidades, parametros,
            protocolos, pruebas, mediciones, activos, usuarios);
    }

    @Test
    void listarParametrosDevuelve404SiNoExisteTipo() {
        when(tipos.existsById(44L)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> service.listarParametros(44L));
    }

    @Test
    void listarParametrosDevuelveListaVaciaCuandoNoHayParametros() {
        when(tipos.existsById(1L)).thenReturn(true);
        when(parametros.findByTipoPruebaIdOrderByIdAsc(1L)).thenReturn(List.of());
        assertTrue(service.listarParametros(1L).isEmpty());
    }

    @Test
    void cambiarEstadoDevuelve404SiNoExisteElProtocolo() {
        when(protocolos.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
            () -> service.cambiarEstado(99L, new CambioEstadoRequest("ANULADO", "Duplicado"), 3L));
        verify(protocolos, never()).save(any());
    }

    @Test
    void crearBorradorRechazaEquipoInexistente() {
        when(activos.obtenerEquipo(8L)).thenThrow(new IllegalArgumentException("No existe el equipo indicado."));
        BorradorRequest request = new BorradorRequest(8L, LocalDateTime.now(),
            new BigDecimal("20"), new BigDecimal("45"), null, List.of(1L));
        assertThrows(IllegalArgumentException.class, () -> service.crearBorrador(request, 3L));
        verify(protocolos, never()).save(any());
    }

    @Test
    void crearBorradorRechazaTipoDePruebaNoCompatible() {
        when(activos.obtenerEquipo(8L)).thenReturn(new ActivosClient.EquipoResumen(
            8L, 2L, 5L, 4L, "Relé de protección", "R-1", "SEL", "751", "S1", "Site", "Parque industrial", "Lampa", "Región Metropolitana"));
        when(compatibilidades.findAllByIdIdTipoEquipo(4L)).thenReturn(List.of());
        BorradorRequest request = new BorradorRequest(8L, LocalDateTime.now(),
            new BigDecimal("20"), new BigDecimal("45"), null, List.of(1L));
        assertThrows(IllegalArgumentException.class, () -> service.crearBorrador(request, 3L));
        verify(protocolos, never()).save(any());
    }

    @Test
    void noPermiteAnularUnProtocoloFormalizado() {
        EstadoProtocolo estado = new EstadoProtocolo(); estado.setCodigo("FORMALIZADO");
        Protocolo protocolo = new Protocolo(); protocolo.setId(1L); protocolo.setIdUsuarioRegistra(3L); protocolo.setEstado(estado);
        when(protocolos.findById(1L)).thenReturn(Optional.of(protocolo));
        assertThrows(IllegalStateException.class,
            () -> service.cambiarEstado(1L, new CambioEstadoRequest("ANULADO", "Corrección"), 3L));
        verify(protocolos, never()).save(any());
    }

    @Test
    void exigeMotivoParaMarcarProtocoloObservado() {
        EstadoProtocolo estado = new EstadoProtocolo(); estado.setCodigo("REGISTRADO_TERRENO");
        Protocolo protocolo = new Protocolo(); protocolo.setId(1L); protocolo.setIdUsuarioRegistra(3L); protocolo.setEstado(estado);
        when(protocolos.findById(1L)).thenReturn(Optional.of(protocolo));
        assertThrows(IllegalArgumentException.class,
            () -> service.cambiarEstado(1L, new CambioEstadoRequest("OBSERVADO", " "), 3L));
        verify(protocolos, never()).save(any());
    }
}
