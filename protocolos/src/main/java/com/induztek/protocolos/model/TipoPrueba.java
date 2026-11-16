package com.induztek.protocolos.model;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="tipo_prueba") @Getter @Setter @NoArgsConstructor
public class TipoPrueba {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_tipo_prueba") private Long id;
    @Column(nullable=false, unique=true, length=140) private String nombre;
}
