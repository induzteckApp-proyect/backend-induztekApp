package com.induztek.usuarios.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="usuarios", uniqueConstraints={
    @UniqueConstraint(name="uk_usuario_correo", columnNames="correo"),
    @UniqueConstraint(name="uk_usuario_rut", columnNames="rut")
})
public class Usuario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=12) private String rut;
    @Column(nullable=false, length=100) private String nombre;
    @Column(length=100) private String apellido;
    @Column(nullable=false, length=160) private String correo;
    @Column(nullable=false, length=20) private String telefono;
    @Column(name="password_hash", length=100) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private RolUsuario rol;
    @Column(nullable=false) private boolean activo=true;
    @Column(length=150) private String giro;
    @Column(name="contacto_nombre", length=120) private String contactoNombre;
    @Column(name="contacto_cargo", length=120) private String contactoCargo;
    @Column(name="avatar_url", length=500) private String avatarUrl;
    @Column(nullable=false) private LocalDateTime creadoEn=LocalDateTime.now();
    @Column(nullable=false) private boolean debeActivar=true;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getRut(){return rut;} public void setRut(String v){rut=v;}
    public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
    public String getApellido(){return apellido;} public void setApellido(String v){apellido=v;}
    public String getCorreo(){return correo;} public void setCorreo(String v){correo=v;}
    public String getTelefono(){return telefono;} public void setTelefono(String v){telefono=v;}
    public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
    public RolUsuario getRol(){return rol;} public void setRol(RolUsuario v){rol=v;}
    public boolean isActivo(){return activo;} public void setActivo(boolean v){activo=v;}
    public String getGiro(){return giro;} public void setGiro(String v){giro=v;}
    public String getContactoNombre(){return contactoNombre;} public void setContactoNombre(String v){contactoNombre=v;}
    public String getContactoCargo(){return contactoCargo;} public void setContactoCargo(String v){contactoCargo=v;}
    public String getAvatarUrl(){return avatarUrl;} public void setAvatarUrl(String v){avatarUrl=v;}
    public LocalDateTime getCreadoEn(){return creadoEn;} public void setCreadoEn(LocalDateTime v){creadoEn=v;}
    public boolean isDebeActivar(){return debeActivar;} public void setDebeActivar(boolean v){debeActivar=v;}
}
