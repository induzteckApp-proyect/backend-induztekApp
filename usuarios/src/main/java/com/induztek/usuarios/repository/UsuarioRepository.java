package com.induztek.usuarios.repository;
import com.induztek.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
 Optional<Usuario> findByCorreoIgnoreCase(String correo);
 boolean existsByCorreoIgnoreCase(String correo);
 boolean existsByRutIgnoreCase(String rut);
}
