package com.induztek.usuarios.dto.response;
public record MensajeResponse(String mensaje, String tokenDesarrollo) {
    public MensajeResponse(String mensaje) { this(mensaje, null); }
}
