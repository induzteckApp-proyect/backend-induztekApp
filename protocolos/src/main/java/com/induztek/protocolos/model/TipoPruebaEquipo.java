package com.induztek.protocolos.model;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
@Entity @Table(name="tipo_prueba_equipo") @Getter @Setter @NoArgsConstructor
public class TipoPruebaEquipo {
    @EmbeddedId private TipoPruebaEquipoId id;
    @MapsId("idTipoPrueba") @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="id_tipo_prueba") private TipoPrueba tipoPrueba;
    // id_tipo_equipo es una referencia externa convencional: NO es clave foránea.
    @Column(name="id_tipo_equipo", insertable=false, updatable=false, nullable=false) private Long idTipoEquipo;
    public TipoPruebaEquipo(TipoPruebaEquipoId id, TipoPrueba tipoPrueba) { this.id=id; this.tipoPrueba=tipoPrueba; }
}
