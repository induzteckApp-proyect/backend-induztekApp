package com.induztek.protocolos.repository;
import com.induztek.protocolos.model.Parametro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ParametroRepository extends JpaRepository<Parametro, Long> { List<Parametro> findByTipoPruebaIdOrderByIdAsc(Long idTipoPrueba); boolean existsByTipoPruebaIdAndCodigo(Long idTipoPrueba, String codigo); }
