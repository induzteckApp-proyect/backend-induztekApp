package com.induztek.activos.repository;
import com.induztek.activos.model.TipoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TipoEquipoRepository extends JpaRepository<TipoEquipo, Long> { Optional<TipoEquipo> findByNombreIgnoreCase(String nombre); List<TipoEquipo> findAllByOrderByNombreAsc(); }
