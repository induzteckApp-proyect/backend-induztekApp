package com.induztek.activos.service;

import com.induztek.activos.client.UsuarioClient;
import com.induztek.activos.dto.request.EquipoRequest;
import com.induztek.activos.dto.request.InstalacionRequest;
import com.induztek.activos.dto.response.ActivoResponses.*;
import com.induztek.activos.model.*;
import com.induztek.activos.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ActivosService {
    private final RegionRepository regiones;
    private final ComunaRepository comunas;
    private final TipoEquipoRepository tipos;
    private final InstalacionRepository instalaciones;
    private final EquipoRepository equipos;
    private final UsuarioClient usuarios;

    public ActivosService(RegionRepository regiones, ComunaRepository comunas,
                          TipoEquipoRepository tipos, InstalacionRepository instalaciones,
                          EquipoRepository equipos, UsuarioClient usuarios) {
        this.regiones = regiones;
        this.comunas = comunas;
        this.tipos = tipos;
        this.instalaciones = instalaciones;
        this.equipos = equipos;
        this.usuarios = usuarios;
    }

    public List<RegionResponse> listarRegiones() {
        return regiones.findAll().stream().map(r -> new RegionResponse(r.getId(), r.getNombre())).toList();
    }

    @Transactional
    public List<ComunaResponse> listarComunas() {
        return comunas.findAllByOrderByNombreAsc().stream()
            .map(c -> new ComunaResponse(c.getId(), c.getNombre(), c.getRegion().getId(), c.getRegion().getNombre())).toList();
    }

    public List<TipoEquipoResponse> listarTiposEquipo() {
        return tipos.findAllByOrderByNombreAsc().stream()
            .map(t -> new TipoEquipoResponse(t.getId(), t.getNombre(), t.getClasificacion())).toList();
    }

    @Transactional
    public InstalacionResponse crearInstalacion(InstalacionRequest request) {
        usuarios.obtenerCliente(request.idUsuarioCliente());
        Comuna comuna = comunas.findById(request.idComuna())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comuna no encontrada."));
        Instalacion instalacion = new Instalacion();
        instalacion.setIdUsuarioCliente(request.idUsuarioCliente());
        instalacion.setNombre(request.nombre().trim());
        instalacion.setUbicacion(limpiar(request.ubicacion()));
        instalacion.setComuna(comuna);
        instalacion.setObservacionesAcceso(limpiar(request.observacionesAcceso()));
        return convertir(instalaciones.save(instalacion));
    }

    @Transactional
    public List<InstalacionResponse> listarInstalacionesCliente(Long idCliente) {
        return instalaciones.findByIdUsuarioClienteOrderByNombreAsc(idCliente).stream().map(this::convertir).toList();
    }

    @Transactional
    public List<ClienteActivosResponse> listarClientesConActivos(String filtro) {
        String texto = filtro == null ? "" : filtro.trim().toLowerCase(java.util.Locale.ROOT);
        return usuarios.listarClientes().stream()
            .filter(UsuarioClient.UsuarioResumen::activo)
            .filter(cliente -> texto.isBlank()
                || contiene(cliente.nombre(), texto)
                || contiene(cliente.correo(), texto)
                || contiene(cliente.rut(), texto))
            .map(cliente -> {
                List<InstalacionConEquiposResponse> lista = instalaciones
                    .findByIdUsuarioClienteOrderByNombreAsc(cliente.id()).stream()
                    .map(instalacion -> new InstalacionConEquiposResponse(
                        convertir(instalacion),
                        equipos.findByInstalacionIdOrderByTagAsc(instalacion.getId()).stream().map(this::convertir).toList()))
                    .toList();
                return new ClienteActivosResponse(cliente.id(), cliente.rut(), cliente.nombre(), cliente.correo(), lista);
            })
            .toList();
    }

    @Transactional
    public InstalacionResponse obtenerInstalacion(Long id) {
        return convertir(instalaciones.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instalación no encontrada.")));
    }

    @Transactional
    public EquipoResponse crearEquipo(EquipoRequest request) {
        Instalacion instalacion = instalaciones.findById(request.idInstalacion())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instalación no encontrada."));
        TipoEquipo tipo = tipos.findById(request.idTipoEquipo())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de equipo no encontrado."));
        if (equipos.existsByInstalacionIdAndTagIgnoreCase(instalacion.getId(), request.tag().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El tag ya existe en esta instalación.");
        }
        Equipo equipo = new Equipo();
        equipo.setInstalacion(instalacion);
        equipo.setTag(request.tag().trim());
        equipo.setTipoEquipo(tipo);
        equipo.setMarca(limpiar(request.marca()));
        equipo.setModelo(limpiar(request.modelo()));
        equipo.setNumeroSerie(limpiar(request.numeroSerie()));
        equipo.setPotenciaKva(request.potenciaKva());
        equipo.setTensiones(request.tensiones());
        equipo.setCorriente(request.corriente());
        equipo.setMaxContactosUohm(request.maxContactosUohm());
        return convertir(equipos.save(equipo));
    }

    @Transactional
    public List<EquipoResponse> listarEquiposInstalacion(Long idInstalacion) {
        if (!instalaciones.existsById(idInstalacion)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instalación no encontrada.");
        }
        return equipos.findByInstalacionIdOrderByTagAsc(idInstalacion).stream().map(this::convertir).toList();
    }

    @Transactional
    public EquipoResponse obtenerEquipo(Long id) {
        return convertir(equipos.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado.")));
    }

    @Transactional
    public List<EquipoInternoResponse> buscarEquiposInternos(String tag, String nombreInstalacion) {
        return equipos.findAllByTagIgnoreCase(tag).stream()
            .filter(e -> nombreInstalacion == null || e.getInstalacion().getNombre().equalsIgnoreCase(nombreInstalacion))
            .map(e -> new EquipoInternoResponse(e.getId(), e.getInstalacion().getId(),
                e.getInstalacion().getIdUsuarioCliente(), e.getTipoEquipo().getId(),
                e.getTipoEquipo().getNombre(), e.getTag(), e.getMarca(), e.getModelo(),
                e.getNumeroSerie(), e.getInstalacion().getNombre(), e.getInstalacion().getUbicacion(), e.getInstalacion().getComuna().getNombre(), e.getInstalacion().getComuna().getRegion().getNombre())).toList();
    }

    public TipoEquipoResponse obtenerTipoEquipoInterno(Long id) {
        TipoEquipo tipo = tipos.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de equipo no encontrado."));
        return new TipoEquipoResponse(tipo.getId(), tipo.getNombre(), tipo.getClasificacion());
    }

    public List<TipoEquipoResponse> listarTiposEquipoInternos() {
        return listarTiposEquipo();
    }

    @Transactional
    public EquipoInternoResponse obtenerEquipoInterno(Long id) {
        Equipo e = equipos.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipo no encontrado."));
        return new EquipoInternoResponse(e.getId(), e.getInstalacion().getId(),
            e.getInstalacion().getIdUsuarioCliente(), e.getTipoEquipo().getId(),
            e.getTipoEquipo().getNombre(), e.getTag(), e.getMarca(), e.getModelo(), e.getNumeroSerie(), e.getInstalacion().getNombre(), e.getInstalacion().getUbicacion(), e.getInstalacion().getComuna().getNombre(), e.getInstalacion().getComuna().getRegion().getNombre());
    }

    private InstalacionResponse convertir(Instalacion i) {
        return new InstalacionResponse(i.getId(), i.getIdUsuarioCliente(), i.getNombre(), i.getUbicacion(),
            i.getComuna().getId(), i.getComuna().getNombre(), i.getComuna().getRegion().getId(),
            i.getComuna().getRegion().getNombre(), i.getObservacionesAcceso());
    }

    private EquipoResponse convertir(Equipo e) {
        return new EquipoResponse(e.getId(), e.getInstalacion().getId(), e.getTag(),
            e.getTipoEquipo().getId(), e.getTipoEquipo().getNombre(), e.getTipoEquipo().getClasificacion(),
            e.getMarca(), e.getModelo(), e.getNumeroSerie(), e.getPotenciaKva(), e.getTensiones(),
            e.getCorriente(), e.getMaxContactosUohm());
    }

    private boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase(java.util.Locale.ROOT).contains(texto);
    }

    private String limpiar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
