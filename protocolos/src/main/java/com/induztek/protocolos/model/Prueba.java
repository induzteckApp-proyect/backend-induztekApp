package com.induztek.protocolos.model;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="prueba") @Getter @Setter @NoArgsConstructor
public class Prueba {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_prueba") private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="id_protocolo", nullable=false) private Protocolo protocolo;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="id_tipo_prueba", nullable=false) private TipoPrueba tipoPrueba;
    @Column(length=120) private String elemento;
    @Column(nullable=false, length=20) private String momento="UNICA";
    @Column(nullable=false) private boolean realizada;
    @Column(name="motivo_no_realizada", columnDefinition="TEXT") private String motivoNoRealizada;
    // 1=Aprobado, 2=Observado, 3=Rechazado. Conserva los identificadores del DBHelper.
    @Column(name="id_resultado") private Integer idResultado;
}
