package com.induztek.protocolos.service;

import com.induztek.protocolos.client.ActivosClient;
import com.induztek.protocolos.client.ActivosClient.EquipoResumen;
import com.induztek.protocolos.client.UsuariosClient;
import com.induztek.protocolos.dto.request.*;
import com.induztek.protocolos.dto.response.ProtocoloResponses.*;
import com.induztek.protocolos.model.*;
import com.induztek.protocolos.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProtocolosService {
    private final EstadoProtocoloRepository estados;
    private final TipoPruebaRepository tiposPrueba;
    private final TipoPruebaEquipoRepository compatibilidades;
    private final ParametroRepository parametros;
    private final ProtocoloRepository protocolos;
    private final PruebaRepository pruebas;
    private final MedicionRepository mediciones;
    private final ActivosClient activos;
    private final UsuariosClient usuarios;

    public ProtocolosService(EstadoProtocoloRepository estados, TipoPruebaRepository tiposPrueba,
                             TipoPruebaEquipoRepository compatibilidades, ParametroRepository parametros,
                             ProtocoloRepository protocolos, PruebaRepository pruebas,
                             MedicionRepository mediciones, ActivosClient activos, UsuariosClient usuarios) {
        this.estados = estados;
        this.tiposPrueba = tiposPrueba;
        this.compatibilidades = compatibilidades;
        this.parametros = parametros;
        this.protocolos = protocolos;
        this.pruebas = pruebas;
        this.mediciones = mediciones;
        this.activos = activos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<TipoPruebaResponse> listarTiposPrueba(Long idTipoEquipo) {
        if (idTipoEquipo == null) {
            return tiposPrueba.findAllByOrderByNombreAsc().stream()
                .map(t -> new TipoPruebaResponse(t.getId(), t.getNombre())).toList();
        }
        activos.obtenerTipoEquipo(idTipoEquipo);
        return compatibilidades.findAllByIdIdTipoEquipo(idTipoEquipo).stream()
            .map(TipoPruebaEquipo::getTipoPrueba)
            .sorted(Comparator.comparing(TipoPrueba::getNombre))
            .map(t -> new TipoPruebaResponse(t.getId(), t.getNombre())).toList();
    }

    @Transactional(readOnly = true)
    public List<ParametroResponse> listarParametros(Long idTipoPrueba) {
        if (!tiposPrueba.existsById(idTipoPrueba)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de prueba no encontrado.");
        }
        return parametros.findByTipoPruebaIdOrderByIdAsc(idTipoPrueba).stream()
            .map(p -> new ParametroResponse(p.getId(), p.getTipoPrueba().getId(),
                p.getCodigo(), p.getNombre(), p.getUnidad())).toList();
    }

    @Transactional
    public ProtocoloResponse crearBorrador(BorradorRequest request, Long idTecnico) {
        EquipoResumen equipo = activos.obtenerEquipo(request.idEquipo());
        List<Long> seleccionados = request.idsTiposPrueba() == null ? List.of()
            : request.idsTiposPrueba().stream().distinct().toList();
        Set<Long> compatibles = compatibilidades.findAllByIdIdTipoEquipo(equipo.idTipoEquipo()).stream()
            .map(c -> c.getId().getIdTipoPrueba()).collect(Collectors.toSet());
        if (!compatibles.containsAll(seleccionados)) {
            throw new IllegalArgumentException("Una o más pruebas seleccionadas no son compatibles con el tipo de equipo.");
        }
        Protocolo protocolo = new Protocolo();
        protocolo.setNumero(generarNumero(request.fechaHoraPrueba()));
        protocolo.setIdEquipo(equipo.idEquipo());
        protocolo.setIdUsuarioRegistra(idTecnico);
        protocolo.setFechaHoraPrueba(request.fechaHoraPrueba());
        protocolo.setTemperaturaC(request.temperaturaC());
        protocolo.setHumedadPct(request.humedadPct());
        protocolo.setObservaciones(limpiar(request.observaciones()));
        protocolo.setEstado(obtenerEstado("BORRADOR"));
        protocolo = protocolos.save(protocolo);

        for (Long idTipo : seleccionados) {
            TipoPrueba tipo = tiposPrueba.findById(idTipo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de prueba no encontrado."));
            Prueba prueba = new Prueba();
            prueba.setProtocolo(protocolo);
            prueba.setTipoPrueba(tipo);
            prueba.setMomento("UNICA");
            prueba.setRealizada(false);
            prueba.setMotivoNoRealizada("PENDIENTE");
            pruebas.save(prueba);
        }
        return convertir(protocolo);
    }

    @Transactional
    public ProtocoloResponse actualizarCabeceraBorrador(Long idProtocolo, BorradorRequest request, Long idTecnico) {
        Protocolo protocolo = obtenerPropio(idProtocolo, idTecnico);
        if (!protocolo.getEstado().getCodigo().equals("BORRADOR")) {
            throw new IllegalStateException("Solo se puede actualizar la cabecera de un borrador.");
        }
        if (!protocolo.getIdEquipo().equals(request.idEquipo())) {
            throw new IllegalArgumentException("No se puede cambiar el equipo de un borrador existente.");
        }
        protocolo.setTemperaturaC(request.temperaturaC());
        protocolo.setHumedadPct(request.humedadPct());
        return convertir(protocolos.save(protocolo));
    }

    @Transactional
    public ProtocoloResponse guardarPruebasSeleccionadas(Long idProtocolo, List<Long> idsTiposPrueba, Long idTecnico) {
        Protocolo protocolo = obtenerPropio(idProtocolo, idTecnico);
        if (!protocolo.getEstado().getCodigo().equals("BORRADOR")) {
            throw new IllegalStateException("Solo se pueden agregar pruebas seleccionadas a un borrador.");
        }
        EquipoResumen equipo = activos.obtenerEquipo(protocolo.getIdEquipo());
        Set<Long> compatibles = compatibilidades.findAllByIdIdTipoEquipo(equipo.idTipoEquipo()).stream()
            .map(c -> c.getId().getIdTipoPrueba()).collect(Collectors.toSet());
        List<Long> seleccionados = idsTiposPrueba == null ? List.of() : idsTiposPrueba.stream().distinct().toList();
        if (!compatibles.containsAll(seleccionados)) {
            throw new IllegalArgumentException("Una o más pruebas seleccionadas no son compatibles con el equipo.");
        }
        Set<Long> yaSeleccionados = pruebas.findByProtocoloIdOrderByIdAsc(idProtocolo).stream()
            .map(p -> p.getTipoPrueba().getId()).collect(Collectors.toSet());
        for (Long idTipo : seleccionados) {
            if (yaSeleccionados.contains(idTipo)) continue;
            TipoPrueba tipo = tiposPrueba.findById(idTipo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de prueba no encontrado."));
            Prueba nueva = new Prueba();
            nueva.setProtocolo(protocolo);
            nueva.setTipoPrueba(tipo);
            nueva.setMomento("UNICA");
            nueva.setRealizada(false);
            nueva.setMotivoNoRealizada("PENDIENTE");
            pruebas.save(nueva);
        }
        return convertir(protocolo);
    }

    @Transactional
    public ProtocoloResponse guardarPrueba(Long idProtocolo, GuardarPruebaRequest request, Long idTecnico) {
        Protocolo protocolo = protocolos.findById(idProtocolo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Protocolo no encontrado."));
        exigirEstadoEditable(protocolo);
        Prueba prueba;
        if (request.idPrueba() != null) {
            prueba = pruebas.findByIdAndProtocoloId(request.idPrueba(), idProtocolo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prueba no encontrada en el protocolo."));
            if (!prueba.getTipoPrueba().getId().equals(request.idTipoPrueba())) {
                throw new IllegalArgumentException("El tipo de prueba no coincide con el registro seleccionado.");
            }
        } else {
            String elemento = limpiar(request.elemento());
            List<Prueba> existentes = pruebas.findByProtocoloIdOrderByIdAsc(idProtocolo).stream()
                .filter(p -> p.getTipoPrueba().getId().equals(request.idTipoPrueba())).toList();
            prueba = existentes.stream()
                .filter(p -> java.util.Objects.equals(
                    p.getElemento() == null ? null : p.getElemento().trim().toLowerCase(Locale.ROOT),
                    elemento == null ? null : elemento.toLowerCase(Locale.ROOT)))
                .sorted(Comparator.comparing(Prueba::isRealizada).thenComparing(Prueba::getId))
                .findFirst().orElseGet(() -> existentes.stream()
                    .filter(p -> !p.isRealizada() && p.getElemento() == null)
                    .findFirst().orElseGet(() -> {
                        TipoPrueba tipo = tiposPrueba.findById(request.idTipoPrueba())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de prueba no encontrado."));
                        Prueba nueva = new Prueba();
                        nueva.setProtocolo(protocolo);
                        nueva.setTipoPrueba(tipo);
                        return nueva;
                    }));
        }

        if (request.realizada()) {
            if (request.idResultado() == null || request.idResultado() < 1 || request.idResultado() > 3) {
                throw new IllegalArgumentException("Una prueba realizada debe tener un resultado válido: 1, 2 o 3.");
            }
        } else if (request.motivoNoRealizada() == null || request.motivoNoRealizada().isBlank()) {
            throw new IllegalArgumentException("Debes indicar el motivo por el que la prueba no fue realizada.");
        }

        prueba.setElemento(limpiar(request.elemento()));
        prueba.setMomento(request.momento().trim().toUpperCase(Locale.ROOT));
        prueba.setRealizada(request.realizada());
        prueba.setMotivoNoRealizada(request.realizada() ? null : request.motivoNoRealizada().trim());
        prueba.setIdResultado(request.realizada() ? request.idResultado() : null);
        prueba = pruebas.save(prueba);
        mediciones.deleteByPruebaId(prueba.getId());

        if (request.realizada()) {
            Set<Long> parametrosPermitidos = parametros.findByTipoPruebaIdOrderByIdAsc(request.idTipoPrueba())
                .stream().map(Parametro::getId).collect(Collectors.toSet());
            Set<Long> recibidos = new HashSet<>();
            for (MedicionRequest dato : request.mediciones() == null ? List.<MedicionRequest>of() : request.mediciones()) {
                if (!parametrosPermitidos.contains(dato.idParametro())) {
                    throw new IllegalArgumentException("El parámetro indicado no pertenece al tipo de prueba seleccionado.");
                }
                if (!recibidos.add(dato.idParametro())) {
                    throw new IllegalArgumentException("No se puede repetir un parámetro en la misma prueba.");
                }
                Parametro parametro = parametros.findById(dato.idParametro())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parámetro no encontrado."));
                Medicion medicion = new Medicion();
                medicion.setPrueba(prueba);
                medicion.setParametro(parametro);
                medicion.setValor(dato.valor());
                mediciones.save(medicion);
            }
        }
        return convertir(protocolo);
    }

    @Transactional
    public ProtocoloResponse cambiarEstado(Long idProtocolo, CambioEstadoRequest request, Long idTecnico) {
        Protocolo protocolo = protocolos.findById(idProtocolo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Protocolo no encontrado."));
        String actual = protocolo.getEstado().getCodigo();
        String nuevo = request.estado().trim().toUpperCase(Locale.ROOT);
        if (actual.equals(nuevo)) throw new IllegalStateException("El protocolo ya se encuentra en ese estado.");
        boolean permitido = switch (nuevo) {
            case "REGISTRADO_TERRENO" -> actual.equals("BORRADOR") || actual.equals("OBSERVADO");
            case "FORMALIZADO", "OBSERVADO" -> actual.equals("REGISTRADO_TERRENO");
            case "ANULADO" -> !actual.equals("FORMALIZADO") && !actual.equals("ANULADO");
            default -> false;
        };
        if (!permitido) throw new IllegalStateException("No se permite cambiar el estado de " + actual + " a " + nuevo + ".");
        if ((nuevo.equals("OBSERVADO") || nuevo.equals("ANULADO"))
            && (request.motivo() == null || request.motivo().isBlank())) {
            throw new IllegalArgumentException("Debes ingresar un motivo para este cambio de estado.");
        }
        if (nuevo.equals("REGISTRADO_TERRENO")) {
            if (pruebas.countByProtocoloIdAndRealizadaTrueAndIdResultadoIsNotNull(idProtocolo) < 1) {
                throw new IllegalStateException("Se requiere al menos una prueba completa con resultado.");
            }
            if (protocolo.getTemperaturaC().doubleValue() < -10 || protocolo.getTemperaturaC().doubleValue() > 60
                || protocolo.getHumedadPct().doubleValue() < 0 || protocolo.getHumedadPct().doubleValue() > 100) {
                throw new IllegalStateException("Las condiciones ambientales deben estar dentro de los rangos permitidos.");
            }
        }
        protocolo.setEstado(obtenerEstado(nuevo));
        if (nuevo.equals("OBSERVADO") || nuevo.equals("ANULADO")) protocolo.setObservaciones(request.motivo().trim());
        return convertir(protocolos.save(protocolo));
    }

    @Transactional
    public ProtocoloResponse prepararEdicion(Long idProtocolo, Long idTecnico) {
        Protocolo protocolo = obtenerPropio(idProtocolo, idTecnico);
        exigirEstadoEditable(protocolo);
        for (Prueba prueba : pruebas.findByProtocoloIdOrderByIdAsc(idProtocolo)) {
            mediciones.deleteByPruebaId(prueba.getId());
            prueba.setRealizada(false);
            prueba.setIdResultado(null);
            prueba.setMotivoNoRealizada("PENDIENTE");
            pruebas.save(prueba);
        }
        protocolo.setEstado(obtenerEstado("BORRADOR"));
        return convertir(protocolos.save(protocolo));
    }

    @Transactional
    public ProtocoloResponse reemplazar(Long idAnterior, BorradorRequest request, Long idTecnico) {
        Protocolo anterior = obtenerPropio(idAnterior, idTecnico);
        if (anterior.getEstado().getCodigo().equals("FORMALIZADO")
            || anterior.getEstado().getCodigo().equals("ANULADO")) {
            throw new IllegalStateException("No se puede reemplazar un protocolo formalizado o anulado.");
        }
        ProtocoloResponse nuevoResponse = crearBorrador(request, idTecnico);
        Protocolo nuevo = protocolos.findById(nuevoResponse.id()).orElseThrow();
        nuevo.setProtocoloReemplazado(anterior);
        protocolos.save(nuevo);
        anterior.setEstado(obtenerEstado("ANULADO"));
        anterior.setObservaciones("Reemplazado por " + nuevo.getNumero());
        protocolos.save(anterior);
        return convertir(nuevo);
    }

    @Transactional(readOnly = true)
    public ProtocoloResponse obtenerDetalle(Long idProtocolo, Long idUsuario, boolean tecnico) {
        Protocolo protocolo = protocolos.findById(idProtocolo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Protocolo no encontrado."));
        if (!tecnico) {
            EquipoResumen equipo = activos.obtenerEquipo(protocolo.getIdEquipo());
            if (!equipo.idUsuarioCliente().equals(idUsuario)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar protocolos de otro cliente.");
            }
            if (protocolo.getEstado().getCodigo().equals("BORRADOR")
                || protocolo.getEstado().getCodigo().equals("ANULADO")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Protocolo no disponible para consulta del cliente.");
            }
        }
        return convertir(protocolo);
    }

    @Transactional(readOnly = true)
    public List<ProtocoloResponse> listarBorradores(Long idTecnico) {
        return protocolos.findByIdUsuarioRegistraOrderByFechaHoraPruebaDesc(idTecnico).stream()
            .filter(p -> p.getEstado().getCodigo().equals("BORRADOR"))
            .map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public List<ProtocoloResponse> listarHistorialTecnico(Long idTecnico) {
        return protocolos.findAllByOrderByFechaHoraPruebaDesc().stream()
            .map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public List<ProtocoloResponse> listarPorEquipo(Long idEquipo, Long idUsuario, boolean tecnico) {
        EquipoResumen equipo = activos.obtenerEquipo(idEquipo);
        if (!tecnico && !equipo.idUsuarioCliente().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar protocolos de otro cliente.");
        }
        return protocolos.findByIdEquipoOrderByFechaHoraPruebaDesc(idEquipo).stream()
            .filter(p -> tecnico || (!p.getEstado().getCodigo().equals("BORRADOR")
                && !p.getEstado().getCodigo().equals("ANULADO")))
            .map(this::convertir).toList();
    }

    @Transactional(readOnly = true)
    public List<ProtocoloResponse> listarProtocolosCliente(Long idCliente) {
        List<ProtocoloResponse> resultados = new ArrayList<>();
        for (Protocolo protocolo : protocolos.findAllByOrderByFechaHoraPruebaDesc()) {
            String estado = protocolo.getEstado().getCodigo();
            if (estado.equals("BORRADOR") || estado.equals("ANULADO")) continue;
            EquipoResumen equipo = activos.obtenerEquipo(protocolo.getIdEquipo());
            if (equipo.idUsuarioCliente().equals(idCliente)) {
                resultados.add(convertir(protocolo));
            }
        }
        resultados.sort(Comparator.comparing(ProtocoloResponse::fechaHoraPrueba).reversed());
        return resultados;
    }

    private Protocolo obtenerPropio(Long id, Long idTecnico) {
        return protocolos.findByIdAndIdUsuarioRegistra(id, idTecnico)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el protocolo del técnico autenticado."));
    }

    private void exigirEstadoEditable(Protocolo protocolo) {
        String estado = protocolo.getEstado().getCodigo();
        if (estado.equals("FORMALIZADO") || estado.equals("ANULADO")) {
            throw new IllegalStateException("Los protocolos formalizados o anulados no pueden editarse.");
        }
    }

    private EstadoProtocolo obtenerEstado(String codigo) {
        return estados.findByCodigo(codigo)
            .orElseThrow(() -> new IllegalStateException("No existe el estado de protocolo " + codigo + "."));
    }

    @Transactional
    public ProtocoloResponse registrarProtocolo(com.induztek.protocolos.dto.request.RegistrarProtocoloRequest request, Long idTecnico) {
        EquipoResumen equipo = activos.obtenerEquipo(request.idEquipo());
        Set<Long> compatibles = compatibilidades.findAllByIdIdTipoEquipo(equipo.idTipoEquipo()).stream()
            .map(c -> c.getId().getIdTipoPrueba()).collect(Collectors.toSet());
        if (!compatibles.contains(request.idTipoPrueba())) {
            throw new IllegalArgumentException("La prueba seleccionada no es compatible con el equipo.");
        }
        TipoPrueba tipo = tiposPrueba.findById(request.idTipoPrueba())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de prueba no encontrado."));
        Protocolo protocolo = new Protocolo();
        protocolo.setNumero(generarNumero(request.fechaHoraPrueba()));
        protocolo.setIdEquipo(equipo.idEquipo());
        protocolo.setIdUsuarioRegistra(idTecnico);
        protocolo.setFechaHoraPrueba(request.fechaHoraPrueba());
        protocolo.setTemperaturaC(request.temperaturaC());
        protocolo.setHumedadPct(request.humedadPct());
        protocolo.setEstado(obtenerEstado("REGISTRADO_TERRENO"));
        protocolo = protocolos.save(protocolo);

        Prueba prueba = new Prueba();
        prueba.setProtocolo(protocolo);
        prueba.setTipoPrueba(tipo);
        prueba.setElemento(limpiar(request.elemento()));
        prueba.setMomento("UNICA");
        prueba.setRealizada(true);
        prueba = pruebas.save(prueba);
        Map<String, Long> idsParametros = parametros.findByTipoPruebaIdOrderByIdAsc(tipo.getId()).stream()
            .collect(Collectors.toMap(Parametro::getCodigo, Parametro::getId));
        for (Map.Entry<String, java.math.BigDecimal> entrada : request.mediciones().entrySet()) {
            Long idParametro = idsParametros.get(entrada.getKey());
            if (idParametro == null) throw new IllegalArgumentException("No existe el parámetro " + entrada.getKey() + " para esta prueba.");
            Medicion medicion = new Medicion();
            medicion.setPrueba(prueba);
            medicion.setParametro(parametros.findById(idParametro).orElseThrow());
            medicion.setValor(entrada.getValue());
            mediciones.save(medicion);
        }
        return convertir(protocolo);
    }

    private String generarNumero(LocalDateTime fecha) {
        String prefijo = "PT-" + fecha.getYear() + "-";
        int siguiente = protocolos.findTopByNumeroStartingWithOrderByNumeroDesc(prefijo)
            .map(p -> Integer.parseInt(p.getNumero().substring(prefijo.length())) + 1)
            .orElse(1);
        return prefijo + String.format(Locale.ROOT, "%04d", siguiente);
    }

    private String limpiar(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private ProtocoloResponse convertir(Protocolo p) {
        List<PruebaResponse> listaPruebas = pruebas.findByProtocoloIdOrderByIdAsc(p.getId()).stream()
            .map(prueba -> {
                List<MedicionResponse> listaMediciones = mediciones.findByPruebaIdOrderByIdAsc(prueba.getId()).stream()
                    .map(m -> new MedicionResponse(m.getId(), m.getParametro().getId(),
                        m.getParametro().getNombre(), m.getParametro().getUnidad(), m.getValor())).toList();
                String resultado = switch (Objects.requireNonNullElse(prueba.getIdResultado(), 0)) {
                    case 1 -> "Aprobado"; case 2 -> "Observado"; case 3 -> "Rechazado"; default -> "Pendiente";
                };
                return new PruebaResponse(prueba.getId(), prueba.getTipoPrueba().getId(),
                    prueba.getTipoPrueba().getNombre(), prueba.getElemento(), prueba.getMomento(),
                    prueba.isRealizada(), prueba.getMotivoNoRealizada(), prueba.getIdResultado(), resultado, listaMediciones);
            }).toList();
        List<Integer> resultados = listaPruebas.stream().filter(PruebaResponse::realizada)
            .map(PruebaResponse::idResultado).filter(Objects::nonNull).toList();
        String global = resultados.isEmpty() ? "Pendiente"
            : resultados.contains(3) ? "Rechazado" : resultados.contains(2) ? "Observado" : "Aprobado";
        EquipoResumen equipo = activos.obtenerEquipo(p.getIdEquipo());
        UsuariosClient.UsuarioResumen cliente = usuarios.obtenerUsuario(equipo.idUsuarioCliente());
        UsuariosClient.UsuarioResumen tecnico = usuarios.obtenerUsuario(p.getIdUsuarioRegistra());
        return new ProtocoloResponse(p.getId(), p.getNumero(), p.getIdEquipo(), p.getIdUsuarioRegistra(),
            p.getFechaHoraPrueba(), p.getTemperaturaC(), p.getHumedadPct(), p.getObservaciones(),
            p.getEstado().getCodigo(), p.getEstado().getNombre(),
            p.getProtocoloReemplazado() == null ? null : p.getProtocoloReemplazado().getId(), listaPruebas, global,
            equipo.idUsuarioCliente(), cliente == null ? "" : cliente.nombre(), cliente == null ? "" : cliente.correo(),
            equipo.nombreInstalacion(), equipo.ubicacion(), equipo.comuna(), equipo.region(), equipo.tag(),
            equipo.tipoEquipo(), equipo.marca(), equipo.modelo(), equipo.numeroSerie(),
            tecnico == null ? "" : tecnico.nombre(), tecnico == null ? "" : tecnico.correo());
    }
}
