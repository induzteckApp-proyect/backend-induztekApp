package com.induztek.protocolos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ActivosClient {
    private final WebClient client;
    private final String internalToken;
    public ActivosClient(WebClient.Builder builder,
                         @Value("${app.activos.url}") String baseUrl,
                         @Value("${app.internal.token}") String internalToken) {
        this.client = builder.baseUrl(baseUrl).build();
        this.internalToken = internalToken;
    }
    public java.util.List<TipoEquipoResumen> listarTiposEquipo() {
        RuntimeException ultimoError = null;
        for (int intento = 1; intento <= 10; intento++) {
            try {
                java.util.List<TipoEquipoResumen> tipos = client.get().uri("/api/activos/internal/tipos-equipo")
                    .header("X-Internal-Token", internalToken).retrieve()
                    .bodyToFlux(TipoEquipoResumen.class).collectList().block();
                if (tipos != null && !tipos.isEmpty()) return tipos;
                ultimoError = new IllegalStateException("El catálogo de tipos de equipo todavía se está cargando.");
            } catch (RuntimeException ex) {
                ultimoError = ex;
            }
            try { Thread.sleep(2000); }
            catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Se interrumpió la carga de catálogos de Activos.", interrupted);
            }
        }
        throw new IllegalStateException("No fue posible consultar los tipos de equipo de Activos después de varios intentos.", ultimoError);
    }
    public void obtenerTipoEquipo(Long id) {
        try {
            client.get().uri("/api/activos/internal/tipos-equipo/{id}", id)
                .header("X-Internal-Token", internalToken).retrieve().bodyToMono(Object.class).block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de equipo no encontrado.");
        }
    }
    public java.util.List<EquipoResumen> buscarEquipos(String tag, String instalacion) {
        RuntimeException ultimoError = null;
        for (int intento = 1; intento <= 10; intento++) {
            try {
                java.util.List<EquipoResumen> resultado = client.get().uri(uri -> uri.path("/api/activos/internal/equipos")
                    .queryParam("tag", tag).queryParam("instalacion", instalacion).build())
                    .header("X-Internal-Token", internalToken).retrieve()
                    .bodyToFlux(EquipoResumen.class).collectList().block();
                if (resultado != null && !resultado.isEmpty()) return resultado;
                ultimoError = new IllegalStateException("El catálogo de equipos todavía se está cargando.");
            } catch (RuntimeException ex) {
                ultimoError = ex;
            }
            try { Thread.sleep(2000); }
            catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Se interrumpió la consulta de equipos.", interrupted);
            }
        }
        throw new IllegalStateException("No se pudo resolver el equipo semilla " + tag + ".", ultimoError);
    }
    public EquipoResumen obtenerEquipo(Long id) {
        try {
            EquipoResumen equipo = client.get().uri("/api/activos/internal/equipos/{id}", id)
                .header("X-Internal-Token", internalToken).retrieve().bodyToMono(EquipoResumen.class).block();
            if (equipo == null) throw new IllegalArgumentException("El equipo no está disponible.");
            return equipo;
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el equipo indicado.");
        }
    }
    public record TipoEquipoResumen(Long id, String nombre, String clasificacion) { }
    public record EquipoResumen(Long idEquipo, Long idInstalacion, Long idUsuarioCliente,
                                Long idTipoEquipo, String tipoEquipo, String tag,
                                String marca, String modelo, String numeroSerie, String nombreInstalacion, String ubicacion, String comuna, String region) { }
}
