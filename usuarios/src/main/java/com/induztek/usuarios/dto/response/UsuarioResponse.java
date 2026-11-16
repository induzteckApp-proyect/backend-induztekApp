package com.induztek.usuarios.dto.response;
import java.time.LocalDateTime;
public record UsuarioResponse(Long id, String rut, String nombre, String apellido, String correo,
    String telefono, String rol, boolean activo, String giro, String contactoNombre,
    String contactoCargo, String avatarUrl, LocalDateTime creadoEn) { }
