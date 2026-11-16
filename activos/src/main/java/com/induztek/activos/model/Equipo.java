package com.induztek.activos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "equipo", uniqueConstraints = @UniqueConstraint(name = "uk_equipo_instalacion_tag", columnNames = {"id_instalacion", "tag"}))
@Getter @Setter @NoArgsConstructor
public class Equipo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_instalacion", nullable = false)
    private Instalacion instalacion;

    @Column(nullable = false, length = 60)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_equipo", nullable = false)
    private TipoEquipo tipoEquipo;

    @Column(length = 100) private String marca;
    @Column(length = 100) private String modelo;
    @Column(name = "numero_serie", length = 100) private String numeroSerie;
    @Column(name = "potencia_kva", precision = 14, scale = 4) private BigDecimal potenciaKva;
    @Column(precision = 14, scale = 4) private BigDecimal tensiones;
    @Column(precision = 14, scale = 4) private BigDecimal corriente;
    @Column(name = "max_contactos_uohm", precision = 14, scale = 4) private BigDecimal maxContactosUohm;
}
