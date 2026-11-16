package com.induztek.protocolos.model;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.math.BigDecimal;
@Entity @Table(name="medicion", uniqueConstraints=@UniqueConstraint(columnNames={"id_prueba","id_parametro"})) @Getter @Setter @NoArgsConstructor
public class Medicion {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_medicion") private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="id_prueba", nullable=false) private Prueba prueba;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="id_parametro", nullable=false) private Parametro parametro;
    @Column(nullable=false, precision=14, scale=4) private BigDecimal valor;
}
