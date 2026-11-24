package com.induztek.activos.config;

import com.induztek.activos.model.*;
import com.induztek.activos.client.UsuarioClient;
import com.induztek.activos.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class SeedData {
    @Bean
    CommandLineRunner cargarCatalogosYActivos(RegionRepository regiones, ComunaRepository comunas,
            TipoEquipoRepository tipos, InstalacionRepository instalaciones, EquipoRepository equipos, UsuarioClient usuarios) {
        return args -> {
            crearRegion(regiones, comunas, "Región Metropolitana", List.of("Lampa", "San Bernardo", "Til Til", "La Florida", "Puente Alto", "Huechuraba"));
            crearRegion(regiones, comunas, "Región de Valparaíso", List.of("Viña del Mar", "Valparaíso", "Quilpué", "Concón", "Los Andes"));
            crearRegion(regiones, comunas, "Región del Biobío", List.of("Concepción", "Talcahuano", "Coronel"));
            crearRegion(regiones, comunas, "Región de O'Higgins", List.of("Rancagua", "Machalí", "San Fernando"));
            crearRegion(regiones, comunas, "Región del Maule", List.of("Talca", "Curicó", "Linares"));

            crearTipo(tipos, "Transformador", "Primario");
            crearTipo(tipos, "Celda MT", "Secundario");
            crearTipo(tipos, "Interruptor", "Primario");
            crearTipo(tipos, "Relé de protección", "Secundario");
            crearTipo(tipos, "Cable de poder", "Primario");

            java.util.Map<String, Long> clientes = usuarios.esperarClientesSemilla().stream()
                .collect(java.util.stream.Collectors.toMap(UsuarioClient.UsuarioResumen::correo, UsuarioClient.UsuarioResumen::id));
            crearInstalacion(instalaciones, comunas, clientes.get("cliente@ejemplo.cl"), "Planta Lampa", "Sector industrial", "Lampa", "Inducción obligatoria de 30 min");
            crearInstalacion(instalaciones, comunas, clientes.get("jarancibia@svcentral.ejemplo"), "Planta Elevadora Sur", "Sector rural poniente", "San Bernardo", "Sala eléctrica subterránea, sin señal");
            crearInstalacion(instalaciones, comunas, clientes.get("pnavarro@cerroalto.ejemplo"), "Faena Til Til", "Acceso por Ruta 5 Norte", "Til Til", "Sin señal en toda la faena");
            crearInstalacion(instalaciones, comunas, clientes.get("cmendez@losaromos.ejemplo"), "Edificio Clínico", "Zona urbana", "La Florida", "Cortes solo domingos 06:00–10:00");
            crearInstalacion(instalaciones, comunas, clientes.get("lbravo@lomasblancas.ejemplo"), "Planta Molienda", "Sector precordillera", "Puente Alto", "Uso obligatorio de respirador");
            crearInstalacion(instalaciones, comunas, clientes.get("fordenes@andesnorte.ejemplo"), "Site Huechuraba", "Parque empresarial", "Huechuraba", "Acompañamiento permanente del cliente");
            crearEquipo(equipos, instalaciones, tipos, "Planta Lampa", "TR-01", "Transformador", "Rhona", "RH-18733", "2012", "1500", "12", "0", null);
                crearEquipo(equipos, instalaciones, tipos, "Planta Lampa", "TR-02", "Transformador", "Rhona", "RH-21904", "2017", "1000", "12", "0", null);
                crearEquipo(equipos, instalaciones, tipos, "Planta Elevadora Sur", "TR-A", "Transformador", "Tusan", "TS-5521", "2009", "2500", "23", "0", null);
                crearEquipo(equipos, instalaciones, tipos, "Edificio Clínico", "CEL-MT-03", "Celda MT", "Ormazabal", "OR-77812", "2018", null, "12", "630", null);
                crearEquipo(equipos, instalaciones, tipos, "Faena Til Til", "INT-52-1", "Interruptor", "Schneider", "SE-449021", "2015", null, "23", "1250", "45");
                crearEquipo(equipos, instalaciones, tipos, "Planta Molienda", "REL-50/51-A", "Relé de protección", "SEL", "SEL-751-30219", "2020", null, null, "5", null);
                crearEquipo(equipos, instalaciones, tipos, "Site Huechuraba", "CAB-MT-02", "Cable de poder", "Nexans", "NX-2201-B", "2019", null, "15", null, null);
            crearEquipo(equipos, instalaciones, tipos, "Faena Til Til", "TR-SS-01", "Transformador", "ABB", "ABB-1180442", "2011", "5000", "23", "0", null);
        };
    }

    private void crearRegion(RegionRepository regiones, ComunaRepository comunas, String nombre, List<String> nombresComunas) {
        Region region = regiones.findAll().stream().filter(r -> r.getNombre().equalsIgnoreCase(nombre)).findFirst().orElseGet(() -> {
            Region nueva = new Region(); nueva.setNombre(nombre); return regiones.save(nueva);
        });
        for (String nombreComuna : nombresComunas) {
            boolean existe = comunas.findAll().stream().anyMatch(c -> c.getRegion().getId().equals(region.getId()) && c.getNombre().equalsIgnoreCase(nombreComuna));
            if (!existe) { Comuna comuna = new Comuna(); comuna.setRegion(region); comuna.setNombre(nombreComuna); comunas.save(comuna); }
        }
    }
    private void crearTipo(TipoEquipoRepository repo, String nombre, String clasificacion) {
        if (repo.findByNombreIgnoreCase(nombre).isEmpty()) { TipoEquipo tipo = new TipoEquipo(); tipo.setNombre(nombre); tipo.setClasificacion(clasificacion); repo.save(tipo); }
    }
    private void crearInstalacion(InstalacionRepository repo, ComunaRepository comunas, Long idCliente,
                                  String nombre, String ubicacion, String comunaNombre, String observaciones) {
        if (idCliente == null) throw new IllegalStateException("No se pudo resolver el ID del cliente para la instalación " + nombre);
        if (repo.findByIdUsuarioClienteAndNombreIgnoreCase(idCliente, nombre).isPresent()) return;
        Comuna comuna = comunas.findAll().stream().filter(c -> c.getNombre().equalsIgnoreCase(comunaNombre)).findFirst().orElseThrow();
        Instalacion i = new Instalacion(); i.setIdUsuarioCliente(idCliente); i.setNombre(nombre); i.setUbicacion(ubicacion); i.setComuna(comuna); i.setObservacionesAcceso(observaciones); repo.save(i);
    }
    private void crearEquipo(EquipoRepository repo, InstalacionRepository instalaciones, TipoEquipoRepository tipos,
                             String instalacionNombre, String tag, String tipoNombre, String marca, String modelo,
                             String serie, String potencia, String tension, String corriente, String maxContacto) {
        Instalacion instalacion = instalaciones.findAll().stream().filter(i -> i.getNombre().equals(instalacionNombre)).findFirst().orElseThrow();
        if (repo.existsByInstalacionIdAndTagIgnoreCase(instalacion.getId(), tag)) return;
        TipoEquipo tipo = tipos.findByNombreIgnoreCase(tipoNombre).orElseThrow();
        Equipo e = new Equipo(); e.setInstalacion(instalacion); e.setTag(tag); e.setTipoEquipo(tipo); e.setMarca(marca); e.setModelo(modelo); e.setNumeroSerie(serie);
        e.setPotenciaKva(decimal(potencia)); e.setTensiones(decimal(tension)); e.setCorriente(decimal(corriente)); e.setMaxContactosUohm(decimal(maxContacto)); repo.save(e);
    }
    private BigDecimal decimal(String value) { return value == null ? null : new BigDecimal(value); }
}
