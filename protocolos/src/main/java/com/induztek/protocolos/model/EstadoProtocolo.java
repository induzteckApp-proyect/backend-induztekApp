package com.induztek.protocolos.model;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="estado_protocolo") @Getter @Setter @NoArgsConstructor
public class EstadoProtocolo {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_estado") private Long id;
    @Column(nullable=false, unique=true, length=40) private String codigo;
    @Column(nullable=false, length=80) private String nombre;
}
