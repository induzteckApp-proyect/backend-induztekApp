package com.induztek.protocolos.repository;
import com.induztek.protocolos.model.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MedicionRepository extends JpaRepository<Medicion, Long> { List<Medicion> findByPruebaIdOrderByIdAsc(Long idPrueba); void deleteByPruebaId(Long idPrueba); }
