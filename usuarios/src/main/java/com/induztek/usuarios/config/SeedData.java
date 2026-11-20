package com.induztek.usuarios.config;

import com.induztek.usuarios.model.Permiso;
import com.induztek.usuarios.model.Rol;
import com.induztek.usuarios.model.Usuario;
import com.induztek.usuarios.repository.PermisoRepository;
import com.induztek.usuarios.repository.RolRepository;
import com.induztek.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Configuration
public class SeedData {
    private static final String PASSWORD_PRUEBAS = "Abc1234@";

    @Bean
    @Order(1)
    CommandLineRunner cargarRolesPermisosYUsuarios(
            RolRepository roles,
            PermisoRepository permisos,
            UsuarioRepository usuarios,
            PasswordEncoder encoder) {
        return args -> {
            Rol admin = crearRol(roles, "ADMIN", "Administrador");
            Rol cliente = crearRol(roles, "CLIENTE", "Cliente");
            Rol tecnico = crearRol(roles, "TECNICO_INGENIERO", "Técnico / Ingeniero");

            List<Permiso> todos = List.of(
                    crearPermiso(permisos, "LEER", "Consultar datos e historiales"),
                    crearPermiso(permisos, "CREACION", "Registrar instalaciones, equipos y pruebas"),
                    crearPermiso(permisos, "EDICION", "Modificar borradores"),
                    crearPermiso(permisos, "FORMALIZACION", "Formalizar protocolos"),
                    crearPermiso(permisos, "ELIMINACION", "Anular registros"),
                    crearPermiso(permisos, "AGREGAR_PERSONAL", "Agregar una cuenta de técnico/ingeniero")
            );

            admin.setPermisos(Set.copyOf(todos));
            tecnico.setPermisos(Set.copyOf(todos));
            cliente.setPermisos(Set.of(todos.get(0)));
            roles.saveAll(List.of(admin, cliente, tecnico));

            asegurarCuenta(usuarios, encoder, "33333333-3", "Administrador Induztek", null,
                    "admin@induztek.cl", "56933333333", admin, null, null, null);
            asegurarCuenta(usuarios, encoder, "17284519-3", "Matías Godoy Herrera", null,
                    "tecnico@induztek.cl", "56900000101", tecnico, null, null, null);
            asegurarCuenta(usuarios, encoder, "76482915-8", "Envasadora del Pacífico S.A.", null,
                    "cliente@ejemplo.cl", "56900000101", cliente, "Agroindustria",
                    "Marcela Fuentes Rojas", "Jefe de Planta");
            asegurarCuenta(usuarios, encoder, "96731540-0", "Sanitaria Valle Central S.A.", null,
                    "jarancibia@svcentral.ejemplo", "56900000102", cliente, "Servicios sanitarios",
                    "Jorge Arancibia Leiva", "Contacto de operaciones");
            asegurarCuenta(usuarios, encoder, "77105362-9", "Minera Cerro Alto SpA", null,
                    "pnavarro@cerroalto.ejemplo", "56900000103", cliente, "Minería",
                    "Patricio Navarro Soto", "Contacto de faena");
            asegurarCuenta(usuarios, encoder, "76903218-5", "Clínica Los Aromos Ltda.", null,
                    "cmendez@losaromos.ejemplo", "56900000104", cliente, "Salud",
                    "Carolina Méndez Pizarro", "Contacto de mantenimiento");
            asegurarCuenta(usuarios, encoder, "99524170-6", "Cementos Lomas Blancas S.A.", null,
                    "lbravo@lomasblancas.ejemplo", "56900000105", cliente, "Construcción",
                    "Luis Bravo Castillo", "Contacto de planta");
            asegurarCuenta(usuarios, encoder, "78214635-1", "Data Center Andes Norte SpA", null,
                    "fordenes@andesnorte.ejemplo", "56900000106", cliente, "Tecnología",
                    "Felipe Órdenes Vera", "Contacto de infraestructura");
            asegurarCuenta(usuarios, encoder, "19412087-7", "Javiera Tapia Muñoz", null,
                    "jtapia@induztek.ejemplo", "56900000107", tecnico, null, null, null);
            asegurarCuenta(usuarios, encoder, "26104583-4", "Carlos Rondón Pérez", null,
                    "crondon@induztek.ejemplo", "56900000108", tecnico, null, null, null);
            asegurarCuenta(usuarios, encoder, "20137648-3", "Diego Valenzuela Ríos", null,
                    "dvalenzuela@induztek.ejemplo", "56900000109", tecnico, null, null, null);
            asegurarCuenta(usuarios, encoder, "15906334-8", "Andrea Contreras Silva", null,
                    "acontreras@induztek.ejemplo", "56900000110", tecnico, null, null, null);
            asegurarCuenta(usuarios, encoder, "16553270-8", "Sebastián Morales Gutiérrez", null,
                    "smorales@induztek.ejemplo", "56900000111", tecnico, null, null, null);
            asegurarCuenta(usuarios, encoder, "13871905-7", "Ricardo Espinoza Lagos", null,
                    "respinoza@induztek.ejemplo", "56900000112", tecnico, null, null, null);

            for (Usuario usuario : usuarios.findAll()) {
                if (usuario.getRol() == null && usuario.getRolLegacy() != null) {
                    String codigo = usuario.getRolLegacy().equals("TECNICO")
                            ? "TECNICO_INGENIERO"
                            : usuario.getRolLegacy();
                    usuario.setRol(roles.findByCodigo(codigo).orElseThrow());
                    usuarios.save(usuario);
                }
            }
        };
    }

    private Rol crearRol(RolRepository repo, String codigo, String nombre) {
        return repo.findByCodigo(codigo).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setCodigo(codigo);
            rol.setNombre(nombre);
            return repo.save(rol);
        });
    }

    private Permiso crearPermiso(PermisoRepository repo, String codigo, String descripcion) {
        return repo.findByCodigo(codigo).orElseGet(() -> {
            Permiso permiso = new Permiso();
            permiso.setCodigo(codigo);
            permiso.setDescripcion(descripcion);
            return repo.save(permiso);
        });
    }

    private void asegurarCuenta(
            UsuarioRepository repo,
            PasswordEncoder encoder,
            String rut,
            String nombre,
            String apellido,
            String correo,
            String telefono,
            Rol rol,
            String giro,
            String contacto,
            String cargo) {
        Usuario usuario = repo.findByCorreoIgnoreCase(correo).orElseGet(Usuario::new);
        usuario.setRut(rut);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setDebeActivar(false);
        usuario.setGiro(giro);
        usuario.setContactoNombre(contacto);
        usuario.setContactoCargo(cargo);
        usuario.setPasswordHash(encoder.encode(PASSWORD_PRUEBAS));
        repo.save(usuario);
    }
}
