package com.induztek.protocolos.repository;
import com.induztek.protocolos.model.Protocolo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ProtocoloRepository extends JpaRepository<Protocolo, Long> {
    List<Protocolo> findByIdUsuarioRegistraOrderByFechaHoraPruebaDesc(Long idUsuarioRegistra);
    List<Protocolo> findAllByOrderByFechaHoraPruebaDesc();
    List<Protocolo> findByIdEquipoOrderByFechaHoraPruebaDesc(Long idEquipo);
    Optional<Protocolo> findByIdAndIdUsuarioRegistra(Long id, Long idUsuarioRegistra);
    Optional<Protocolo> findTopByNumeroStartingWithOrderByNumeroDesc(String prefijo);
    Optional<Protocolo> findByNumero(String numero);
}
