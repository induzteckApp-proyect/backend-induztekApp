package com.induztek.usuarios.model;

import jakarta.persistence.*;

@Entity
@Table(name = "permiso")
public class Permiso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;
    @Column(length = 180)
    private String descripcion;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
