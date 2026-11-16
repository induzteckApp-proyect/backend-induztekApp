package com.induztek.activos.repository;
import com.induztek.activos.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface EquipoRepository extends JpaRepository<Equipo, Long> { List<Equipo> findByInstalacionIdOrderByTagAsc(Long instalacionId); List<Equipo> findAllByTagIgnoreCase(String tag); boolean existsByInstalacionIdAndTagIgnoreCase(Long instalacionId, String tag); Optional<Equipo> findByIdAndInstalacionId(Long id, Long instalacionId); }
