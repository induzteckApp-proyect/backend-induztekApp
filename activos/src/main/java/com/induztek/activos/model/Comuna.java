package com.induztek.activos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comuna", uniqueConstraints = @UniqueConstraint(columnNames = {"id_region", "nombre"}))
@Getter @Setter @NoArgsConstructor
public class Comuna {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comuna")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_region", nullable = false)
    private Region region;

    @Column(nullable = false, length = 100)
    private String nombre;
}
