package com.induztek.usuarios.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {
    private final JavaMailSender mail;
    private final boolean habilitado;
    private final String remitente;

    public CorreoService(
            JavaMailSender mail,
            @Value("${app.mail.enabled:false}") boolean habilitado,
            @Value("${app.mail.from:}") String remitente,
            @Value("${spring.mail.username:}") String cuentaCorreo) {
        this.mail = mail;
        this.habilitado = habilitado;
        this.remitente = remitente == null || remitente.isBlank() ? cuentaCorreo : remitente;
    }

    public boolean estaHabilitado() {
        return habilitado;
    }

    public void enviar(String destino, String asunto, String cuerpo) {
        if (!habilitado) {
            return;
        }
        if (remitente == null || remitente.isBlank()) {
            throw new IllegalStateException("Configura MAIL_FROM o MAIL_USERNAME antes de enviar correos.");
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destino);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        mail.send(mensaje);
    }
}
