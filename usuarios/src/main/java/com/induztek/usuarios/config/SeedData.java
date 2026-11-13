package com.induztek.usuarios.config;

import com.induztek.usuarios.model.*;
import com.induztek.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedData {
    @Bean
    CommandLineRunner cargarUsuariosPrueba(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            crearSiNoExiste(repo, encoder,
                "17284519-3", "Matías Godoy Herrera", null,
                "tecnico@induztek.cl", "56900000101", RolUsuario.TECNICO_INGENIERO,
                null, null, null);

            crearSiNoExiste(repo, encoder,
                "76482915-8", "Envasadora del Pacífico S.A.", null,
                "cliente@ejemplo.cl", "56900000101", RolUsuario.CLIENTE,
                "Agroindustria", "Marcela Fuentes Rojas", "Jefe de Planta");

            crearSiNoExiste(repo, encoder,
                "33333333-3", "Administrador Induztek", null,
                "admin@induztek.cl", "56933333333", RolUsuario.ADMIN,
                null, null, null);
        };
    }

    private void crearSiNoExiste(
        UsuarioRepository repo, PasswordEncoder encoder,
        String rut, String nombre, String apellido, String correo, String telefono,
        RolUsuario rol, String giro, String contactoNombre, String contactoCargo
    ) {
        if (repo.existsByCorreoIgnoreCase(correo)) return;

        Usuario usuario = new Usuario();
        usuario.setRut(rut);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setDebeActivar(false);
        usuario.setGiro(giro);
        usuario.setContactoNombre(contactoNombre);
        usuario.setContactoCargo(contactoCargo);
        usuario.setPasswordHash(encoder.encode("Abc1234@"));
        repo.save(usuario);
    }
}
