package com.induztek.protocolos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.List;

@Component
public class UsuariosClient {
    private final WebClient client;
    private final String internalToken;
    public UsuariosClient(WebClient.Builder builder,
            @Value("${app.usuarios.url}") String baseUrl,
            @Value("${app.internal.token}") String internalToken) {
        this.client = builder.baseUrl(baseUrl).build();
        this.internalToken = internalToken;
    }
    public UsuarioResumen obtenerUsuario(Long id) {
        try {
            return client.get().uri("/api/internal/usuarios/{id}", id)
                .header("X-Internal-Token", internalToken).retrieve()
                .bodyToMono(UsuarioResumen.class).block();
        } catch (WebClientResponseException.NotFound ex) {
            return null;
        }
    }

    public List<UsuarioResumen> listarUsuarios() {
        RuntimeException ultimoError = null;
        for (int intento = 1; intento <= 10; intento++) {
            try {
                List<UsuarioResumen> resultado = client.get().uri("/api/internal/usuarios")
                    .header("X-Internal-Token", internalToken).retrieve()
                    .bodyToFlux(UsuarioResumen.class).collectList().block();
                if (resultado != null && resultado.stream().anyMatch(u -> u.correo().equals("dvalenzuela@induztek.ejemplo"))) {
                    return resultado;
                }
                ultimoError = new IllegalStateException("Aún no se han cargado todas las cuentas semilla de Usuarios.");
            } catch (RuntimeException ex) {
                ultimoError = ex;
            }
            try { Thread.sleep(2000); }
            catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Se interrumpió la carga de usuarios.", interrupted);
            }
        }
        throw new IllegalStateException("No fue posible consultar los usuarios después de varios intentos.", ultimoError);
    }
    public record UsuarioResumen(Long id, String rut, String nombre, String correo, String rol, boolean activo) { }
}
