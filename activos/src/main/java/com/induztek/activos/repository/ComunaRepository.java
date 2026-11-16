package com.induztek.activos.repository;
import com.induztek.activos.model.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ComunaRepository extends JpaRepository<Comuna, Long> { List<Comuna> findAllByOrderByNombreAsc(); }
