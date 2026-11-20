package com.induztek.usuarios.service;

import com.induztek.usuarios.model.TokenAcceso;
import com.induztek.usuarios.dto.request.CrearUsuarioRequest;
import com.induztek.usuarios.dto.request.LoginRequest;
import com.induztek.usuarios.dto.request.RecuperarRequest;
import com.induztek.usuarios.dto.request.ActualizarPerfilRequest;
import com.induztek.usuarios.model.Rol;
import com.induztek.usuarios.model.Usuario;
import com.induztek.usuarios.repository.RolRepository;
import com.induztek.usuarios.repository.UsuarioRepository;
import com.induztek.usuarios.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock UsuarioRepository usuarios;
    @Mock RolRepository roles;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @Mock TokenService tokens;
    @Mock CorreoService correo;
    private UsuarioService service;

    @BeforeEach
    void configurar() {
        service = new UsuarioService(usuarios, roles, encoder, jwt, tokens, correo);
    }

    @Test
    void crearCuentaDeClienteLaDejaPendienteDeActivacion() {
        Rol cliente = new Rol(); cliente.setCodigo("CLIENTE");
        when(usuarios.existsByCorreoIgnoreCase("nuevo@ejemplo.cl")).thenReturn(false);
        when(usuarios.existsByRutIgnoreCase("17284519-3")).thenReturn(false);
        when(roles.findByCodigo("CLIENTE")).thenReturn(Optional.of(cliente));
        when(encoder.encode(anyString())).thenReturn("hash-aleatorio-no-utilizable");
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> { Usuario u = inv.getArgument(0); u.setId(10L); return u; });
        when(tokens.crear(any(Usuario.class), any())).thenReturn("token-temporal");
        when(correo.estaHabilitado()).thenReturn(false);
        var response = service.crear(new CrearUsuarioRequest("17.284.519-3", "María Pérez", "Pérez",
            "nuevo@ejemplo.cl", "56912345678", "CLIENTE", "Servicios", "María Pérez", "Administradora"));

        assertTrue(response.mensaje().contains("deshabilitado"));
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        assertFalse(captor.getValue().isActivo());
        assertTrue(captor.getValue().isDebeActivar());
        assertEquals("hash-aleatorio-no-utilizable", captor.getValue().getPasswordHash());
    }

    @Test
    void loginRechazaCuentaPendienteDeActivacion() {
        Rol cliente = new Rol(); cliente.setCodigo("CLIENTE");
        Usuario usuario = new Usuario(); usuario.setCorreo("nuevo@ejemplo.cl");
        usuario.setRol(cliente); usuario.setActivo(false); usuario.setDebeActivar(true);
        when(usuarios.findByCorreoIgnoreCase("nuevo@ejemplo.cl")).thenReturn(Optional.of(usuario));
        assertThrows(ResponseStatusException.class,
            () -> service.login(new LoginRequest("nuevo@ejemplo.cl", "Abc1234@")));
    }

    @Test
    void permiteCrearClientePersonaNaturalSinDatosComerciales() {
        Rol cliente = new Rol(); cliente.setCodigo("CLIENTE");
        when(usuarios.existsByCorreoIgnoreCase("persona@ejemplo.cl")).thenReturn(false);
        when(usuarios.existsByRutIgnoreCase("12345678-5")).thenReturn(false);
        when(roles.findByCodigo("CLIENTE")).thenReturn(Optional.of(cliente));
        when(encoder.encode(anyString())).thenReturn("hash-aleatorio");
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokens.crear(any(Usuario.class), any())).thenReturn("token-persona");
        when(correo.estaHabilitado()).thenReturn(false);
        var response = service.crear(new CrearUsuarioRequest("12345678-5", "Persona Natural", "Prueba",
            "persona@ejemplo.cl", "56912345678", "CLIENTE", null, null, null));

        assertTrue(response.mensaje().contains("deshabilitado"));
        verify(usuarios).save(any(Usuario.class));
    }

    @Test
    void loginCorrectoEntregaJwt() {
        Rol tecnico = new Rol(); tecnico.setCodigo("TECNICO_INGENIERO");
        Usuario usuario = new Usuario(); usuario.setId(8L); usuario.setCorreo("tecnico@ejemplo.cl");
        usuario.setRol(tecnico); usuario.setActivo(true); usuario.setDebeActivar(false);
        usuario.setPasswordHash("bcrypt");
        when(usuarios.findByCorreoIgnoreCase("tecnico@ejemplo.cl")).thenReturn(Optional.of(usuario));
        when(encoder.matches("ClaveSegura1!", "bcrypt")).thenReturn(true);
        when(jwt.generar(usuario)).thenReturn("jwt-firmado");

        var response = service.login(new LoginRequest("TECNICO@EJEMPLO.CL", "ClaveSegura1!"));

        assertEquals("jwt-firmado", response.token());
        assertEquals("Bearer", response.tipo());
        assertEquals("TECNICO_INGENIERO", response.usuario().rol());
    }

    @Test
    void activarCuentaDefineHashYHabilitaUsuario() {
        Rol cliente = new Rol(); cliente.setCodigo("CLIENTE");
        Usuario usuario = new Usuario(); usuario.setId(15L); usuario.setRol(cliente);
        usuario.setActivo(false); usuario.setDebeActivar(true);
        when(tokens.consumir("token-invitacion", TokenAcceso.TipoToken.INVITACION)).thenReturn(usuario);
        when(encoder.encode("NuevaClave1!")).thenReturn("nuevo-bcrypt");
        when(usuarios.save(usuario)).thenReturn(usuario);

        var response = service.activar(new com.induztek.usuarios.dto.request.ActivarRequest("token-invitacion", "NuevaClave1!"));

        assertTrue(usuario.isActivo());
        assertFalse(usuario.isDebeActivar());
        assertEquals("nuevo-bcrypt", usuario.getPasswordHash());
        assertEquals(15L, response.id());
    }

    @Test
    void permiteRegistrarClienteEmpresaConDatosComercialesOpcionales() {
        Rol cliente = new Rol();
        cliente.setCodigo("CLIENTE");
        when(usuarios.existsByCorreoIgnoreCase("nuevo@ejemplo.cl")).thenReturn(false);
        when(usuarios.existsByRutIgnoreCase("17284519-3")).thenReturn(false);
        when(roles.findByCodigo("CLIENTE")).thenReturn(Optional.of(cliente));
        when(encoder.encode(anyString())).thenReturn("hash-temporal");
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokens.crear(any(Usuario.class), any())).thenReturn("codigo-temporal");
        when(correo.estaHabilitado()).thenReturn(false);

        var response = service.crear(new CrearUsuarioRequest(
                "17.284.519-3", "Empresa de Prueba", "",
                "nuevo@ejemplo.cl", "56912345678", "CLIENTE",
                "Servicios eléctricos", null, null));

        assertTrue(response.mensaje().contains("deshabilitado"));
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        assertEquals("Servicios eléctricos", captor.getValue().getGiro());
        assertNull(captor.getValue().getContactoNombre());
        assertNull(captor.getValue().getContactoCargo());
    }

    @Test
    void conservaLaCuentaSiFallaElEnvioDelCorreoDeActivacion() {
        Rol tecnico = new Rol();
        tecnico.setCodigo("TECNICO_INGENIERO");
        when(usuarios.existsByCorreoIgnoreCase("nuevo@ejemplo.cl")).thenReturn(false);
        when(usuarios.existsByRutIgnoreCase("17284519-3")).thenReturn(false);
        when(roles.findByCodigo("TECNICO_INGENIERO")).thenReturn(Optional.of(tecnico));
        when(encoder.encode(anyString())).thenReturn("hash-temporal");
        when(usuarios.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            usuario.setId(20L);
            return usuario;
        });
        when(tokens.crear(any(Usuario.class), any())).thenReturn("codigo-privado");
        when(correo.estaHabilitado()).thenReturn(true);
        doThrow(new IllegalStateException("SMTP no disponible"))
                .when(correo).enviar(eq("nuevo@ejemplo.cl"), anyString(), anyString());

        var response = service.crear(new CrearUsuarioRequest(
                "17.284.519-3", "Técnico Nuevo", "Prueba",
                "nuevo@ejemplo.cl", "56912345678", "TECNICO_INGENIERO",
                null, null, null));

        assertTrue(response.mensaje().contains("La cuenta fue creada"));
        assertTrue(response.mensaje().contains("No vuelvas a crearla"));
        verify(usuarios).save(any(Usuario.class));
        verify(correo).enviar(eq("nuevo@ejemplo.cl"), anyString(), contains("codigo-privado"));
    }

    @Test
    void solicitarRecuperacionEnvíaCodigoAlCorreoRegistrado() {
        Usuario usuario = new Usuario();
        usuario.setId(31L);
        usuario.setCorreo("cliente@ejemplo.cl");
        usuario.setActivo(true);
        usuario.setDebeActivar(false);
        when(usuarios.findByCorreoIgnoreCase("cliente@ejemplo.cl")).thenReturn(Optional.of(usuario));
        when(tokens.crear(usuario, TokenAcceso.TipoToken.RECUPERACION)).thenReturn("codigo-recuperacion");
        when(correo.estaHabilitado()).thenReturn(true);

        var response = service.solicitarRecuperacion(new RecuperarRequest("cliente@ejemplo.cl"));

        assertTrue(response.mensaje().contains("código"));
        verify(correo).enviar(eq("cliente@ejemplo.cl"), anyString(), contains("codigo-recuperacion"));
    }

    @Test
    void cambiarContrasenaVerificaClaveActualYGuardaNuevoHash() {
        Rol cliente = new Rol();
        cliente.setCodigo("CLIENTE");
        Usuario usuario = new Usuario();
        usuario.setId(32L);
        usuario.setCorreo("cliente@ejemplo.cl");
        usuario.setRol(cliente);
        usuario.setPasswordHash("hash-anterior");
        when(usuarios.findByCorreoIgnoreCase("cliente@ejemplo.cl")).thenReturn(Optional.of(usuario));
        when(encoder.matches("Actual123@", "hash-anterior")).thenReturn(true);
        when(encoder.encode("NuevaClave1!")).thenReturn("hash-nuevo");
        when(usuarios.save(usuario)).thenReturn(usuario);

        service.cambiarContrasena("cliente@ejemplo.cl",
                new com.induztek.usuarios.dto.request.CambiarContrasenaRequest("Actual123@", "NuevaClave1!"));

        assertEquals("hash-nuevo", usuario.getPasswordHash());
        verify(usuarios).save(usuario);
        verify(correo).enviar(eq("cliente@ejemplo.cl"), anyString(), anyString());
    }

    @Test
    void actualizarPerfilAceptaDatosComercialesOpcionales() {
        Rol cliente = new Rol();
        cliente.setCodigo("CLIENTE");
        Usuario usuario = new Usuario();
        usuario.setId(33L);
        usuario.setCorreo("cliente@ejemplo.cl");
        usuario.setRol(cliente);
        when(usuarios.findByCorreoIgnoreCase("cliente@ejemplo.cl")).thenReturn(Optional.of(usuario));
        when(usuarios.save(usuario)).thenReturn(usuario);

        var response = service.actualizarPerfil("cliente@ejemplo.cl",
                new ActualizarPerfilRequest("María José", "Núñez", "56912345678",
                        "Servicios eléctricos", null, null));

        assertEquals("Servicios eléctricos", response.giro());
        assertNull(response.contactoNombre());
        assertNull(response.contactoCargo());
    }
}
