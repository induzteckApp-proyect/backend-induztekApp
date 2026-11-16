package com.induztek.protocolos.config;

import com.induztek.protocolos.client.ActivosClient;
import com.induztek.protocolos.model.*;
import com.induztek.protocolos.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Configuration
public class SeedData {
    @Bean
    @Order(2)
    CommandLineRunner cargarCatalogos(EstadoProtocoloRepository estados, TipoPruebaRepository tipos,
            ParametroRepository parametros, TipoPruebaEquipoRepository compatibilidades,
            ActivosClient activos) {
        return args -> {
            crearEstado(estados, "BORRADOR", "Borrador");
            crearEstado(estados, "REGISTRADO_TERRENO", "Registrado en terreno");
            crearEstado(estados, "OBSERVADO", "Observado");
            crearEstado(estados, "FORMALIZADO", "Formalizado");
            crearEstado(estados, "ANULADO", "Anulado");

            crearTipo(tipos, "Resistencia de Aislación");
            crearTipo(tipos, "Relación de Transformación (TTR)");
            crearTipo(tipos, "Resistencia de Devanados");
            crearTipo(tipos, "Resistencia de Contactos");
            crearTipo(tipos, "Operación de Relé de Protección");

            crearParametros(tipos, parametros, "Resistencia de Aislación", List.of(
                new String[]{"T_PRUEBA", "Tensión de prueba", "kV"},
                new String[]{"LEC_1MIN", "Lectura a 1 min", "MΩ"},
                new String[]{"LEC_10MIN", "Lectura a 10 min", "MΩ"}));
            crearParametros(tipos, parametros, "Relación de Transformación (TTR)", List.of(
                new String[]{"POS_TAP", "Posición de TAP", "posición"},
                new String[]{"REL_TEORICA", "Relación teórica", "ratio"},
                new String[]{"REL_MEDIDA", "Relación medida", "ratio"}));
            crearParametros(tipos, parametros, "Resistencia de Devanados", List.of(
                new String[]{"AT_BT", "Resistencia AT-BT", "mΩ"},
                new String[]{"AT_T", "Resistencia AT-T", "mΩ"},
                new String[]{"BT_T", "Resistencia BT-T", "mΩ"}));

           crearParametros(tipos, parametros, "Resistencia de Contactos",
            Collections.singletonList(
                new String[]{"RES_CONTACTO", "Resistencia de contactos", "µΩ"}));

            /*crearParametros(tipos, parametros, "Resistencia de Contactos", List.of(
                new String[]{"RES_CONTACTO", "Resistencia de contactos", "µΩ"})); */

            crearParametros(tipos, parametros, "Operación de Relé de Protección", List.of(
                new String[]{"CORRIENTE_AJUSTE", "Corriente de ajuste", "A"},
                new String[]{"CORRIENTE_MEDIDA", "Corriente medida", "A"},
                new String[]{"TIEMPO_ESPERADO", "Tiempo esperado", "ms"},
                new String[]{"TIEMPO_MEDIDO", "Tiempo medido", "ms"}));

            Map<String, Long> idsTipoEquipo = activos.listarTiposEquipo().stream()
                .collect(java.util.stream.Collectors.toMap(ActivosClient.TipoEquipoResumen::nombre, ActivosClient.TipoEquipoResumen::id));
            crearCompatibilidad(tipos, compatibilidades, idsTipoEquipo, "Transformador", List.of(
                "Resistencia de Aislación", "Relación de Transformación (TTR)", "Resistencia de Devanados"));
            crearCompatibilidad(tipos, compatibilidades, idsTipoEquipo, "Celda MT", List.of(
                "Resistencia de Aislación", "Resistencia de Contactos"));
            crearCompatibilidad(tipos, compatibilidades, idsTipoEquipo, "Interruptor", List.of(
                "Resistencia de Aislación", "Resistencia de Contactos"));
            crearCompatibilidad(tipos, compatibilidades, idsTipoEquipo, "Relé de protección", List.of(
                "Operación de Relé de Protección"));
            crearCompatibilidad(tipos, compatibilidades, idsTipoEquipo, "Cable de poder", List.of(
                "Resistencia de Aislación"));
        };
    }

    private void crearEstado(EstadoProtocoloRepository repo, String codigo, String nombre) {
        if (repo.findByCodigo(codigo).isEmpty()) {
            EstadoProtocolo e = new EstadoProtocolo();
            e.setCodigo(codigo);
            e.setNombre(nombre);
            repo.save(e);
        }
    }

    private void crearTipo(TipoPruebaRepository repo, String nombre) {
        if (repo.findByNombreIgnoreCase(nombre).isEmpty()) {
            TipoPrueba t = new TipoPrueba();
            t.setNombre(nombre);
            repo.save(t);
        }
    }

    private void crearParametros(TipoPruebaRepository tipos, ParametroRepository parametros,
                                 String nombreTipo, List<String[]> datos) {
        TipoPrueba tipo = tipos.findByNombreIgnoreCase(nombreTipo).orElseThrow();
        for (String[] dato : datos) {
            if (!parametros.existsByTipoPruebaIdAndCodigo(tipo.getId(), dato[0])) {
                Parametro p = new Parametro();
                p.setTipoPrueba(tipo);
                p.setCodigo(dato[0]);
                p.setNombre(dato[1]);
                p.setUnidad(dato[2]);
                parametros.save(p);
            }
        }
    }

    private void crearCompatibilidad(TipoPruebaRepository tipos, TipoPruebaEquipoRepository repo,
                                     Map<String, Long> idsEquipo, String nombreEquipo, List<String> pruebas) {
        Long idEquipo = idsEquipo.get(nombreEquipo);
        if (idEquipo == null) return;
        for (String nombrePrueba : pruebas) {
            TipoPrueba tipo = tipos.findByNombreIgnoreCase(nombrePrueba).orElseThrow();
            TipoPruebaEquipoId id = new TipoPruebaEquipoId(idEquipo, tipo.getId());
            if (!repo.existsById(id)) repo.save(new TipoPruebaEquipo(id, tipo));
        }
    }
}
