package com.induztek.activos.repository;
import com.induztek.activos.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RegionRepository extends JpaRepository<Region, Long> { boolean existsByNombreIgnoreCase(String nombre); }
