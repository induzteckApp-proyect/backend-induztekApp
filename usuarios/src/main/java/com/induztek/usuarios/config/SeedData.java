package com.induztek.usuarios.config;

import com.induztek.usuarios.model.*;
import com.induztek.usuarios.repository.*;
import com.induztek.usuarios.service.CorreoService;
import com.induztek.usuarios.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Configuration
public class SeedData {
    private static final Logger log = LoggerFactory.getLogger(SeedData.class);

    @Bean
    @Order(1)
    CommandLineRunner cargarRolesPermisosYUsuarios(RolRepository roles, PermisoRepository permisos,
            UsuarioRepository usuarios, PasswordEncoder encoder, TokenService tokens,
            CorreoService correo, @Value("${app.tokens.expose-in-development:false}") boolean exponerTokens) {
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

            // Se conserva la cuenta de prueba que ya existía en el microservicio.
            crearCuentaSiNoExiste(usuarios, encoder, "33333333-3", "Administrador Induztek", null,
                "admin@induztek.cl", "56933333333", admin, null, null, null, true);

            // Migración de las 13 cuentas originales del DBHelper. Si una cuenta ya existía
            // en Usuarios se conserva su ID, pero se restablece el acceso para exigir activación.
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "17284519-3", "Matías Godoy Herrera", "tecnico@induztek.cl", "56900000101", tecnico, null, null, null);
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "76482915-8", "Envasadora del Pacífico S.A.", "cliente@ejemplo.cl", "56900000101", cliente, "Agroindustria", "Marcela Fuentes Rojas", "Jefe de Planta");
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "96731540-0", "Sanitaria Valle Central S.A.", "jarancibia@svcentral.ejemplo", "56900000102", cliente, "Servicios sanitarios", "Jorge Arancibia Leiva", "Contacto de operaciones");
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "77105362-9", "Minera Cerro Alto SpA", "pnavarro@cerroalto.ejemplo", "56900000103", cliente, "Minería", "Patricio Navarro Soto", "Contacto de faena");
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "76903218-5", "Clínica Los Aromos Ltda.", "cmendez@losaromos.ejemplo", "56900000104", cliente, "Salud", "Carolina Méndez Pizarro", "Contacto de mantenimiento");
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "99524170-6", "Cementos Lomas Blancas S.A.", "lbravo@lomasblancas.ejemplo", "56900000105", cliente, "Construcción", "Luis Bravo Castillo", "Contacto de planta");
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "78214635-1", "Data Center Andes Norte SpA", "fordenes@andesnorte.ejemplo", "56900000106", cliente, "Tecnología", "Felipe Órdenes Vera", "Contacto de infraestructura");
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "19412087-7", "Javiera Tapia Muñoz", "jtapia@induztek.ejemplo", "56900000107", tecnico, null, null, null);
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "26104583-4", "Carlos Rondón Pérez", "crondon@induztek.ejemplo", "56900000108", tecnico, null, null, null);
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "20137648-3", "Diego Valenzuela Ríos", "dvalenzuela@induztek.ejemplo", "56900000109", tecnico, null, null, null);
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "15906334-8", "Andrea Contreras Silva", "acontreras@induztek.ejemplo", "56900000110", tecnico, null, null, null);
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "16553270-8", "Sebastián Morales Gutiérrez", "smorales@induztek.ejemplo", "56900000111", tecnico, null, null, null);
            migrarCuenta(usuarios, encoder, tokens, correo, exponerTokens, "13871905-7", "Ricardo Espinoza Lagos", "respinoza@induztek.ejemplo", "56900000112", tecnico, null, null, null);

            // Los registros heredados de una versión ENUM se enlazan a la tabla ROL.
            for (Usuario usuario : usuarios.findAll()) {
                if (usuario.getRol() == null && usuario.getRolLegacy() != null) {
                    String codigo = usuario.getRolLegacy().equals("TECNICO") ? "TECNICO_INGENIERO" : usuario.getRolLegacy();
                    usuario.setRol(roles.findByCodigo(codigo).orElseThrow());
                    usuarios.save(usuario);
                }
            }
        };
    }

    private Rol crearRol(RolRepository repo, String codigo, String nombre) {
        return repo.findByCodigo(codigo).orElseGet(() -> {
            Rol rol = new Rol(); rol.setCodigo(codigo); rol.setNombre(nombre); return repo.save(rol);
        });
    }
    private Permiso crearPermiso(PermisoRepository repo, String codigo, String descripcion) {
        return repo.findByCodigo(codigo).orElseGet(() -> {
            Permiso permiso = new Permiso(); permiso.setCodigo(codigo); permiso.setDescripcion(descripcion); return repo.save(permiso);
        });
    }
    private void crearCuentaSiNoExiste(UsuarioRepository repo, PasswordEncoder encoder, String rut,
            String nombre, String apellido, String correo, String telefono, Rol rol,
            String giro, String contacto, String cargo, boolean activa) {
        if (repo.existsByCorreoIgnoreCase(correo)) return;
        Usuario usuario = new Usuario();
        usuario.setRut(rut); usuario.setNombre(nombre); usuario.setApellido(apellido);
        usuario.setCorreo(correo); usuario.setTelefono(telefono); usuario.setRol(rol);
        usuario.setActivo(activa); usuario.setDebeActivar(!activa);
        usuario.setGiro(giro); usuario.setContactoNombre(contacto); usuario.setContactoCargo(cargo);
        usuario.setPasswordHash(encoder.encode("Abc1234@"));
        repo.save(usuario);
    }
    private void migrarCuenta(UsuarioRepository repo, PasswordEncoder encoder, TokenService tokens, CorreoService correo,
            boolean exponerTokens, String rut, String nombre, String email,
            String telefono, Rol rol, String giro, String contacto, String cargo) {
        Usuario existente = repo.findByCorreoIgnoreCase(email).orElse(null);
        // Si ya tiene relación ROL, esta cuenta ya fue migrada y no se reinicia en cada arranque.
        if (existente != null && existente.getRol() != null) return;
        Usuario usuario = existente == null ? new Usuario() : existente;
        usuario.setRut(rut);
        usuario.setNombre(nombre);
        usuario.setCorreo(email);
        usuario.setTelefono(telefono);
        usuario.setRol(rol);
        usuario.setActivo(false);
        usuario.setDebeActivar(true);
        usuario.setGiro(giro);
        usuario.setContactoNombre(contacto);
        usuario.setContactoCargo(cargo);
        // Los hashes SHA-256 de SQLite no se reutilizan. Se genera un BCrypt aleatorio
        // que no puede utilizarse para iniciar sesión; la activación lo reemplaza.
        usuario.setPasswordHash(encoder.encode(java.util.UUID.randomUUID().toString()));
        usuario = repo.save(usuario);
        String token = tokens.crear(usuario, TokenAcceso.TipoToken.INVITACION);
        correo.enviar(email, "Activa tu cuenta Induztek", "Tus datos fueron migrados desde la aplicación anterior. Define una nueva contraseña con este enlace: " + correo.enlace("activar", token));
        if (!correo.estaHabilitado() && exponerTokens) {
            log.warn("Token de activación de migración para {}: {}", email, token);
        }
    }
}
