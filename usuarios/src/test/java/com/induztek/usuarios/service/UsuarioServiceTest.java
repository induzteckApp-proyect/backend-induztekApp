package com.induztek.usuarios.service;

import com.induztek.usuarios.model.TokenAcceso;
import com.induztek.usuarios.dto.request.CrearUsuarioRequest;
import com.induztek.usuarios.dto.request.LoginRequest;
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
        service = new UsuarioService(usuarios, roles, encoder, jwt, tokens, correo, true);
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
        when(correo.enlace("activar", "token-temporal")).thenReturn("http://localhost/activar?token=token-temporal");

        var response = service.crear(new CrearUsuarioRequest("17.284.519-3", "María Pérez", "Pérez",
            "nuevo@ejemplo.cl", "56912345678", "CLIENTE", "Servicios", "María Pérez", "Administradora"));

        assertFalse(response.tokenDesarrollo().isBlank());
        assertEquals("token-temporal", response.tokenDesarrollo());
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
        when(correo.enlace("activar", "token-persona")).thenReturn("http://localhost/activar");

        var response = service.crear(new CrearUsuarioRequest("12345678-5", "Persona Natural", "Prueba",
            "persona@ejemplo.cl", "56912345678", "CLIENTE", null, null, null));

        assertEquals("token-persona", response.tokenDesarrollo());
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
    void rechazaDatosDeClienteIncompletos() {
        Rol cliente = new Rol(); cliente.setCodigo("CLIENTE");
        when(usuarios.existsByCorreoIgnoreCase("nuevo@ejemplo.cl")).thenReturn(false);
        when(usuarios.existsByRutIgnoreCase("17284519-3")).thenReturn(false);
        when(roles.findByCodigo("CLIENTE")).thenReturn(Optional.of(cliente));
        assertThrows(IllegalArgumentException.class, () -> service.crear(new CrearUsuarioRequest(
            "17.284.519-3", "María Pérez", "Pérez", "nuevo@ejemplo.cl", "56912345678",
            "CLIENTE", "Servicios", null, null)));
        verify(usuarios, never()).save(any());
    }
}
