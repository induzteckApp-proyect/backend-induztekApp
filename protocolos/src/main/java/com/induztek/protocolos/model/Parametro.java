package com.induztek.protocolos.model;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="parametro", uniqueConstraints=@UniqueConstraint(columnNames={"id_tipo_prueba","codigo"})) @Getter @Setter @NoArgsConstructor
public class Parametro {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_parametro") private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="id_tipo_prueba", nullable=false) private TipoPrueba tipoPrueba;
    @Column(nullable=false, length=50) private String codigo;
    @Column(nullable=false, length=120) private String nombre;
    @Column(nullable=false, length=10) private String unidad;
}
