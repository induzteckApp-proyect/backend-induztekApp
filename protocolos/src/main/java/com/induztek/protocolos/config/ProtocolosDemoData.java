
package com.induztek.protocolos.config;

import com.induztek.protocolos.client.ActivosClient;
import com.induztek.protocolos.client.UsuariosClient;
import com.induztek.protocolos.model.*;
import com.induztek.protocolos.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class ProtocolosDemoData {

    @Bean
    @Order(3)
    CommandLineRunner cargarProtocolosDemo(
            ProtocoloRepository protocolos,
            PruebaRepository pruebas,
            MedicionRepository mediciones,
            EstadoProtocoloRepository estados,
            TipoPruebaRepository tipos,
            ParametroRepository parametros,
            ActivosClient activos,
            UsuariosClient usuarios) {

        return args -> {
            Map<String, Long> idUsuarios = usuarios.listarUsuarios().stream()
                .collect(Collectors.toMap(
                    UsuariosClient.UsuarioResumen::correo,
                    UsuariosClient.UsuarioResumen::id
                ));

            crearProtocolo(protocolos, pruebas, mediciones, estados, tipos, parametros, activos, idUsuarios,
                "PT-2024-0312", "TR-01", "Planta Lampa", "tecnico@induztek.cl",
                "2024-09-14T10:30:00", "18", "55", "Protocolo histórico de transformador.", "FORMALIZADO",
                "Resistencia de Aislación", 1, "12500", "31250");

            crearProtocolo(protocolos, pruebas, mediciones, estados, tipos, parametros, activos, idUsuarios,
                "PT-2025-0288", "TR-01", "Planta Lampa", "jtapia@induztek.ejemplo",
                "2025-09-13T10:30:00", "21", "48", "Protocolo histórico de transformador.", "FORMALIZADO",
                "Resistencia de Aislación", 1, "9800", "21560");

            crearProtocolo(protocolos, pruebas, mediciones, estados, tipos, parametros, activos, idUsuarios,
                "PT-2026-0401", "TR-01", "Planta Lampa", "tecnico@induztek.cl",
                "2026-09-26T10:30:00", "19", "60", "Lecturas de aislación con resultado observado.", "REGISTRADO_TERRENO",
                "Resistencia de Aislación", 2, "6100", "10980");

            crearProtocolo(protocolos, pruebas, mediciones, estados, tipos, parametros, activos, idUsuarios,
                "PT-2026-0402", "TR-02", "Planta Lampa", "tecnico@induztek.cl",
                "2026-09-26T11:30:00", "19", "60", "Protocolo de transformador.", "REGISTRADO_TERRENO",
                "Resistencia de Aislación", 1, null, null);

            crearProtocolo(protocolos, pruebas, mediciones, estados, tipos, parametros, activos, idUsuarios,
                "PT-2026-0389", "CEL-MT-03", "Edificio Clínico", "crondon@induztek.ejemplo",
                "2026-09-20T09:00:00", "22", "45", "Resultado observado.", "OBSERVADO",
                "Resistencia de Contactos", 2, null, null);

            crearProtocolo(protocolos, pruebas, mediciones, estados, tipos, parametros, activos, idUsuarios,
                "PT-2026-0377", "REL-50/51-A", "Planta Molienda", "dvalenzuela@induztek.ejemplo",
                "2026-09-15T10:00:00", "24", "35", "Protocolo formalizado.", "FORMALIZADO",
                "Operación de Relé de Protección", 1, null, null);

            crearProtocolo(protocolos, pruebas, mediciones, estados, tipos, parametros, activos, idUsuarios,
                "PT-2026-0360", "INT-52-1", "Faena Til Til", "jtapia@induztek.ejemplo",
                "2026-09-08T08:00:00", "27", "20", "Resultado rechazado.", "FORMALIZADO",
                "Resistencia de Contactos", 3, null, null);
        };
    }

    private void crearProtocolo(
            ProtocoloRepository protocolos,
            PruebaRepository pruebas,
            MedicionRepository mediciones,
            EstadoProtocoloRepository estados,
            TipoPruebaRepository tipos,
            ParametroRepository parametros,
            ActivosClient activos,
            Map<String, Long> idUsuarios,
            String numero,
            String tag,
            String nombreInstalacion,
            String correoTecnico,
            String fecha,
            String temperatura,
            String humedad,
            String observaciones,
            String estado,
            String nombrePrueba,
            int resultado,
            String lectura1,
            String lectura10) {

        // Evitar duplicar protocolos que ya existen en la base de datos.
        if (protocolos.findByNumero(numero).isPresent()) {
            System.out.println("Protocolo semilla ya existente, se omite: " + numero);
            return;
        }

        ActivosClient.EquipoResumen equipo = activos.buscarEquipos(tag, nombreInstalacion).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No se encontró el equipo semilla " + tag + " en " + nombreInstalacion
            ));

        Long idTecnico = idUsuarios.get(correoTecnico);

        if (idTecnico == null) {
            throw new IllegalStateException(
                "No se encontró el usuario técnico semilla " + correoTecnico
            );
        }

        Protocolo protocolo = new Protocolo();
        protocolo.setNumero(numero);
        protocolo.setIdEquipo(equipo.idEquipo());
        protocolo.setIdUsuarioRegistra(idTecnico);
        protocolo.setFechaHoraPrueba(LocalDateTime.parse(fecha));
        protocolo.setTemperaturaC(new BigDecimal(temperatura));
        protocolo.setHumedadPct(new BigDecimal(humedad));
        protocolo.setObservaciones(observaciones);
        protocolo.setEstado(estados.findByCodigo(estado).orElseThrow());

        protocolo = protocolos.save(protocolo);

        TipoPrueba tipo = tipos.findByNombreIgnoreCase(nombrePrueba).orElseThrow();

        Prueba prueba = new Prueba();
        prueba.setProtocolo(protocolo);
        prueba.setTipoPrueba(tipo);
        prueba.setMomento("UNICA");
        prueba.setRealizada(true);
        prueba.setIdResultado(resultado);

        if (nombrePrueba.equals("Resistencia de Aislación")) {
            prueba.setElemento("Devanado AT-BT");
        }

        prueba = pruebas.save(prueba);

        if (lectura1 != null && lectura10 != null) {
            insertarMedicion(mediciones, parametros, prueba, tipo, "T_PRUEBA", "5.0");
            insertarMedicion(mediciones, parametros, prueba, tipo, "LEC_1MIN", lectura1);
            insertarMedicion(mediciones, parametros, prueba, tipo, "LEC_10MIN", lectura10);
        }
    }

    private void insertarMedicion(
            MedicionRepository mediciones,
            ParametroRepository parametros,
            Prueba prueba,
            TipoPrueba tipo,
            String codigo,
            String valor) {

        Parametro parametro = parametros
            .findByTipoPruebaIdOrderByIdAsc(tipo.getId()).stream()
            .filter(p -> p.getCodigo().equals(codigo))
            .findFirst()
            .orElseThrow();

        Medicion medicion = new Medicion();
        medicion.setPrueba(prueba);
        medicion.setParametro(parametro);
        medicion.setValor(new BigDecimal(valor));

        mediciones.save(medicion);
    }
}
