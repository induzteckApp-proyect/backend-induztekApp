package com.induztek.usuarios.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", uniqueConstraints = {
    @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo"),
    @UniqueConstraint(name = "uk_usuario_rut", columnNames = "rut")
})
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 12) private String rut;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(length = 100) private String apellido;
    @Column(nullable = false, length = 160) private String correo;
    @Column(nullable = false, length = 20) private String telefono;
    @Column(name = "password_hash", length = 100) private String passwordHash;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol")
    private Rol rol;
    // Campo transitorio de compatibilidad con instalaciones antiguas que conservan
    // la columna rol. La relación id_rol es la fuente oficial desde esta versión.
    @Column(name = "rol", length = 30) private String rolLegacy;
    @Column(nullable = false) private boolean activo = true;
    @Column(length = 150) private String giro;
    @Column(name = "contacto_nombre", length = 120) private String contactoNombre;
    @Column(name = "contacto_cargo", length = 120) private String contactoCargo;
    @Column(name = "avatar_url", length = 500) private String avatarUrl;
    @Column(nullable = false) private LocalDateTime creadoEn = LocalDateTime.now();
    @Column(nullable = false) private boolean debeActivar = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; this.rolLegacy = rol == null ? null : rol.getCodigo(); }
    public String getRolLegacy() { return rolLegacy; }
    public void setRolLegacy(String rolLegacy) { this.rolLegacy = rolLegacy; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public String getGiro() { return giro; }
    public void setGiro(String giro) { this.giro = giro; }
    public String getContactoNombre() { return contactoNombre; }
    public void setContactoNombre(String contactoNombre) { this.contactoNombre = contactoNombre; }
    public String getContactoCargo() { return contactoCargo; }
    public void setContactoCargo(String contactoCargo) { this.contactoCargo = contactoCargo; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public boolean isDebeActivar() { return debeActivar; }
    public void setDebeActivar(boolean debeActivar) { this.debeActivar = debeActivar; }
}
