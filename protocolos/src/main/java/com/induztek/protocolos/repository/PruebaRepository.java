package com.induztek.protocolos.repository;
import com.induztek.protocolos.model.Prueba;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PruebaRepository extends JpaRepository<Prueba, Long> {
    List<Prueba> findByProtocoloIdOrderByIdAsc(Long idProtocolo);
    Optional<Prueba> findByIdAndProtocoloId(Long id, Long idProtocolo);
    long countByProtocoloIdAndRealizadaTrueAndIdResultadoIsNotNull(Long idProtocolo);
}
