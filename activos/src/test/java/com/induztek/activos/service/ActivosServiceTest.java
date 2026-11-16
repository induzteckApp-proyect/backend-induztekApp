package com.induztek.activos.service;

import com.induztek.activos.client.UsuarioClient;
import com.induztek.activos.dto.request.EquipoRequest;
import com.induztek.activos.dto.request.InstalacionRequest;
import com.induztek.activos.model.*;
import com.induztek.activos.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivosServiceTest {
    @Mock RegionRepository regiones;
    @Mock ComunaRepository comunas;
    @Mock TipoEquipoRepository tipos;
    @Mock InstalacionRepository instalaciones;
    @Mock EquipoRepository equipos;
    @Mock UsuarioClient usuarios;
    private ActivosService service;

    @BeforeEach
    void configurar() {
        service = new ActivosService(regiones, comunas, tipos, instalaciones, equipos, usuarios);
    }

    @Test
    void crearInstalacionValidaClienteYGuardaRelacionGeografica() {
        Region region = new Region(); region.setId(4L); region.setNombre("Región Metropolitana");
        Comuna comuna = new Comuna(); comuna.setId(9L); comuna.setNombre("Lampa"); comuna.setRegion(region);
        when(usuarios.obtenerCliente(2L)).thenReturn(new UsuarioClient.UsuarioResumen(2L, "76482915-8", "Cliente", "cliente@ejemplo.cl", "CLIENTE", true));
        when(comunas.findById(9L)).thenReturn(Optional.of(comuna));
        when(instalaciones.save(any(Instalacion.class))).thenAnswer(inv -> {
            Instalacion i = inv.getArgument(0); i.setId(11L); return i;
        });

        var response = service.crearInstalacion(new InstalacionRequest(2L, "Planta", "Sector norte", 9L, "Acceso controlado"));

        assertEquals(11L, response.id());
        assertEquals(2L, response.idUsuarioCliente());
        assertEquals("Lampa", response.comuna());
        verify(usuarios).obtenerCliente(2L);
    }

    @Test
    void crearInstalacionFallaSiComunaNoExiste() {
        when(usuarios.obtenerCliente(2L)).thenReturn(new UsuarioClient.UsuarioResumen(2L, "76482915-8", "Cliente", "cliente@ejemplo.cl", "CLIENTE", true));
        when(comunas.findById(90L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
            () -> service.crearInstalacion(new InstalacionRequest(2L, "Planta", null, 90L, null)));
        verify(instalaciones, never()).save(any());
    }

    @Test
    void crearEquipoRechazaTagDuplicadoDentroDeLaInstalacion() {
        Instalacion instalacion = new Instalacion(); instalacion.setId(3L);
        TipoEquipo tipo = new TipoEquipo(); tipo.setId(1L);
        when(instalaciones.findById(3L)).thenReturn(Optional.of(instalacion));
        when(tipos.findById(1L)).thenReturn(Optional.of(tipo));
        when(equipos.existsByInstalacionIdAndTagIgnoreCase(3L, "TR-01")).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> service.crearEquipo(
            new EquipoRequest(3L, "TR-01", 1L, null, null, null, null, null, null, null)));
        verify(equipos, never()).save(any());
    }

    @Test
    void listarRegionesDevuelveCatalogo() {
        Region region = new Region(); region.setId(1L); region.setNombre("Región Metropolitana");
        when(regiones.findAll()).thenReturn(java.util.List.of(region));
        var response = service.listarRegiones();
        assertEquals(1, response.size());
        assertEquals("Región Metropolitana", response.get(0).nombre());
    }

    @Test
    void crearEquipoFallaSiLaInstalacionNoExiste() {
        when(instalaciones.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.crearEquipo(
            new EquipoRequest(99L, "TR-99", 1L, null, null, null, null, null, null, null)));
        verify(tipos, never()).findById(anyLong());
        verify(equipos, never()).save(any());
    }

    @Test
    void crearEquipoGuardaDatosTecnicos() {
        Instalacion instalacion = new Instalacion(); instalacion.setId(3L);
        TipoEquipo tipo = new TipoEquipo(); tipo.setId(1L); tipo.setNombre("Transformador"); tipo.setClasificacion("Primario");
        when(instalaciones.findById(3L)).thenReturn(Optional.of(instalacion));
        when(tipos.findById(1L)).thenReturn(Optional.of(tipo));
        when(equipos.existsByInstalacionIdAndTagIgnoreCase(3L, "TR-02")).thenReturn(false);
        when(equipos.save(any(Equipo.class))).thenAnswer(inv -> { Equipo e = inv.getArgument(0); e.setId(22L); return e; });
        var response = service.crearEquipo(new EquipoRequest(3L, "TR-02", 1L, "ABB", "TX-100", "SN-1",
            new BigDecimal("1000"), new BigDecimal("12"), BigDecimal.ZERO, null));
        assertEquals(22L, response.id());
        assertEquals("Transformador", response.tipoEquipo());
        assertEquals(new BigDecimal("1000"), response.potenciaKva());
    }
}
