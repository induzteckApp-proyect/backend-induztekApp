package com.induztek.activos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class UsuarioClient {
    private final WebClient webClient;
    private final String internalToken;

    public UsuarioClient(WebClient.Builder builder,
                         @Value("${app.usuarios.url}") String baseUrl,
                         @Value("${app.internal.token}") String internalToken) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.internalToken = internalToken;
    }

    public java.util.List<UsuarioResumen> listarClientes() {
        RuntimeException ultimoError = null;
        for (int intento = 1; intento <= 10; intento++) {
            try {
                java.util.List<UsuarioResumen> resultado = webClient.get()
                    .uri(uri -> uri.path("/api/internal/usuarios").queryParam("rol", "CLIENTE").build())
                    .header("X-Internal-Token", internalToken).retrieve()
                    .bodyToFlux(UsuarioResumen.class).collectList().block();
                if (resultado != null && resultado.size() >= 6) return resultado;
                ultimoError = new IllegalStateException("Aún no se han cargado todas las cuentas de clientes semilla.");
            } catch (RuntimeException ex) {
                ultimoError = ex;
            }
            try { Thread.sleep(2000); }
            catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Se interrumpió la carga de usuarios desde Usuarios.", interrupted);
            }
        }
        throw new IllegalStateException("No fue posible consultar los clientes en Usuarios.", ultimoError);
    }

    public UsuarioResumen obtenerCliente(Long idUsuario) {
        try {
            UsuarioResumen usuario = webClient.get()
                .uri("/api/internal/usuarios/{id}", idUsuario)
                .header("X-Internal-Token", internalToken)
                .retrieve()
                .bodyToMono(UsuarioResumen.class)
                .block();
            if (usuario == null || !"CLIENTE".equals(usuario.rol()) || !usuario.activo()) {
                throw new IllegalArgumentException("El usuario indicado no es un cliente activo.");
            }
            return usuario;
        } catch (WebClientResponseException.NotFound ex) {
            throw new IllegalArgumentException("No existe el cliente indicado.");
        }
    }

    public record UsuarioResumen(Long id, String rut, String nombre, String correo, String rol, boolean activo) { }
}
