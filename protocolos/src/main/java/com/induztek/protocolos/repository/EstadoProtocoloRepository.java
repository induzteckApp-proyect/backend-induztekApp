package com.induztek.protocolos.repository;
import com.induztek.protocolos.model.EstadoProtocolo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface EstadoProtocoloRepository extends JpaRepository<EstadoProtocolo, Long> { Optional<EstadoProtocolo> findByCodigo(String codigo); }
