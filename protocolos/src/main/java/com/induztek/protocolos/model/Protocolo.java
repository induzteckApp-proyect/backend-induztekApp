package com.induztek.protocolos.model;
import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.math.BigDecimal; import java.time.LocalDateTime;
@Entity @Table(name="protocolo") @Getter @Setter @NoArgsConstructor
public class Protocolo {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id_protocolo") private Long id;
    @Column(nullable=false, unique=true, length=40) private String numero;
    // Identificadores externos. No existen FKs hacia Activos ni Usuarios.
    @Column(name="id_equipo", nullable=false) private Long idEquipo;
    @Column(name="id_usuario_registra", nullable=false) private Long idUsuarioRegistra;
    @Column(name="fecha_hora_prueba", nullable=false) private LocalDateTime fechaHoraPrueba;
    @Column(name="temperatura_c", nullable=false, precision=8, scale=2) private BigDecimal temperaturaC;
    @Column(name="humedad_pct", nullable=false, precision=8, scale=2) private BigDecimal humedadPct;
    @Column(columnDefinition="TEXT") private String observaciones;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="id_estado", nullable=false) private EstadoProtocolo estado;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="id_protocolo_reemplazado") private Protocolo protocoloReemplazado;
}
