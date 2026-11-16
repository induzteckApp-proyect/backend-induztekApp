package com.induztek.usuarios.dto.response;
public record AuthResponse(String token, String tipo, UsuarioResponse usuario) { }
