package com.induztek.activos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "instalacion")
@Getter @Setter @NoArgsConstructor
public class Instalacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_instalacion")
    private Long id;

    @Column(name = "id_usuario_cliente", nullable = false)
    private Long idUsuarioCliente;

    @Column(nullable = false, length = 140)
    private String nombre;

    @Column(length = 250)
    private String ubicacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_comuna", nullable = false)
    private Comuna comuna;

    @Column(name = "observaciones_acceso", columnDefinition = "TEXT")
    private String observacionesAcceso;
}
