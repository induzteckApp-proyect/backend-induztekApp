
package com.induztek.usuarios.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class CorreoService {

    private final JavaMailSender mail;
    private final boolean habilitado;
    private final String remitente;
    private final String urlFrontend;

    public CorreoService(
            JavaMailSender mail,
            @Value("${app.mail.enabled:false}") boolean habilitado,
            @Value("${app.mail.from:no-reply@induztek.cl}") String remitente,
            @Value("${app.frontend.url:http://localhost:3000}") String urlFrontend
    ) {
        this.mail = mail;
        this.habilitado = habilitado;
        this.remitente = remitente;
        this.urlFrontend = urlFrontend;
    }

    public boolean estaHabilitado() {
        return habilitado;
    }

    public void enviar(String destino, String asunto, String cuerpo) {
        if (!habilitado) return;

        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(remitente);
        m.setTo(destino);
        m.setSubject(asunto);
        m.setText(cuerpo);

        mail.send(m);
    }

    public String enlace(String accion, String token) {
        return urlFrontend + "/" + accion
                + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
