package com.induztek.protocolos.repository;
import com.induztek.protocolos.model.TipoPruebaEquipo;
import com.induztek.protocolos.model.TipoPruebaEquipoId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TipoPruebaEquipoRepository extends JpaRepository<TipoPruebaEquipo, TipoPruebaEquipoId> { List<TipoPruebaEquipo> findAllByIdIdTipoEquipo(Long idTipoEquipo); }
