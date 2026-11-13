package com.induztek.usuarios.repository;
import com.induztek.usuarios.model.TokenAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface TokenAccesoRepository extends JpaRepository<TokenAcceso,Long> {
 Optional<TokenAcceso> findByTokenHashAndTipoAndUsadoFalse(String hash, TokenAcceso.TipoToken tipo);
 void deleteByUsuarioIdAndTipo(Long usuarioId, TokenAcceso.TipoToken tipo);
}
