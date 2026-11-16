package com.induztek.protocolos.model;
import jakarta.persistence.Column; import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode; import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.io.Serializable;
@Embeddable @Getter @Setter @NoArgsConstructor @EqualsAndHashCode
public class TipoPruebaEquipoId implements Serializable {
    @Column(name="id_tipo_equipo") private Long idTipoEquipo;
    @Column(name="id_tipo_prueba") private Long idTipoPrueba;
    public TipoPruebaEquipoId(Long idTipoEquipo, Long idTipoPrueba) { this.idTipoEquipo=idTipoEquipo; this.idTipoPrueba=idTipoPrueba; }
}
