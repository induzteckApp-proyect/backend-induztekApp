package com.induztek.protocolos.repository;
import com.induztek.protocolos.model.TipoPrueba;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.Optional;
public interface TipoPruebaRepository extends JpaRepository<TipoPrueba, Long> { List<TipoPrueba> findAllByOrderByNombreAsc(); Optional<TipoPrueba> findByNombreIgnoreCase(String nombre); }
