package com.induztek.activos.repository;
import com.induztek.activos.model.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InstalacionRepository extends JpaRepository<Instalacion, Long> { List<Instalacion> findByIdUsuarioClienteOrderByNombreAsc(Long idUsuarioCliente);
    java.util.Optional<Instalacion> findByIdUsuarioClienteAndNombreIgnoreCase(Long idUsuarioCliente, String nombre); }
