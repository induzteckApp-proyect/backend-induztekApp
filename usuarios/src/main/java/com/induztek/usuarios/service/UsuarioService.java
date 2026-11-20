package com.induztek.usuarios.service;

import com.induztek.usuarios.dto.request.*;
import com.induztek.usuarios.dto.response.*;
import com.induztek.usuarios.model.*;
import com.induztek.usuarios.repository.RolRepository;
import com.induztek.usuarios.repository.UsuarioRepository;
import com.induztek.usuarios.security.JwtService;
import com.induztek.usuarios.security.PasswordPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UsuarioService {
    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final TokenService tokens;
    private final CorreoService correo;
    public UsuarioService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder,
                          JwtService jwt, TokenService tokens, CorreoService correo) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
        this.jwt = jwt;
        this.tokens = tokens;
        this.correo = correo;
    }

    @Transactional
    public MensajeResponse crear(CrearUsuarioRequest request) {
        String correoNormalizado = normalizar(request.correo());
        if (usuarios.existsByCorreoIgnoreCase(correoNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado.");
        }
        String rut = normalizarRut(request.rut());
        if (!rutValido(rut)) throw new IllegalArgumentException("El RUT no es válido.");
        if (usuarios.existsByRutIgnoreCase(rut)) throw new ResponseStatusException(HttpStatus.CONFLICT, "El RUT ya está registrado.");

        Rol rol = roles.findByCodigo(request.rol())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol indicado no existe."));
        Usuario usuario = new Usuario();
        usuario.setRut(rut);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(vacioANull(request.apellido()));
        usuario.setCorreo(correoNormalizado);
        usuario.setTelefono(request.telefono());
        usuario.setRol(rol);
        usuario.setActivo(false);
        usuario.setDebeActivar(true);
        usuario.setPasswordHash(encoder.encode(UUID.randomUUID().toString()));
        boolean esCliente = usuario.getRol().getCodigo().equals("CLIENTE");
        usuario.setGiro(esCliente ? vacioANull(request.giro()) : null);
        usuario.setContactoNombre(esCliente ? vacioANull(request.contactoNombre()) : null);
        usuario.setContactoCargo(esCliente ? vacioANull(request.contactoCargo()) : null);
        usuario = usuarios.save(usuario);

        String token = tokens.crear(usuario, TokenAcceso.TipoToken.INVITACION);
        if (correo.estaHabilitado()) {
            try {
                correo.enviar(correoNormalizado, "Activa tu cuenta Induztek",
                    "Tu cuenta fue creada por un administrador. Ingresa este código en Induztek App para definir tu contraseña.\n\nCódigo de activación: "
                        + token + "\n\nEl código vence en 30 minutos y solo puede utilizarse una vez.");
                return new MensajeResponse("Cuenta creada y correo de activación enviado correctamente.");
            } catch (RuntimeException ex) {
                log.error("La cuenta {} fue creada, pero no se pudo enviar el correo de activación.", correoNormalizado, ex);
                return new MensajeResponse("La cuenta fue creada, pero no se pudo enviar el correo de activación. No vuelvas a crearla; solicita al administrador que revise la configuración del correo.");
            }
        }
        return new MensajeResponse("Cuenta creada. El envío de correo está deshabilitado; configura SMTP para completar la activación.");
    }

    @Transactional
    public UsuarioResponse activar(ActivarRequest request) {
        PasswordPolicy.exigir(request.nuevaContrasena());
        Usuario usuario = tokens.consumir(request.token(), TokenAcceso.TipoToken.INVITACION);
        usuario.setPasswordHash(encoder.encode(request.nuevaContrasena()));
        usuario.setActivo(true);
        usuario.setDebeActivar(false);
        return respuesta(usuarios.save(usuario));
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByCorreoIgnoreCase(normalizar(request.correo()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));
        if (!usuario.isActivo() || usuario.isDebeActivar() || usuario.getPasswordHash() == null
            || !encoder.matches(request.contrasena(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Correo o contraseña incorrectos, o cuenta pendiente de activación.");
        }
        return new AuthResponse(jwt.generar(usuario), "Bearer", respuesta(usuario));
    }

    @Transactional
    public MensajeResponse solicitarRecuperacion(RecuperarRequest request) {
        Usuario usuario = usuarios.findByCorreoIgnoreCase(normalizar(request.correo()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El correo no se encuentra registrado."));
        if (!usuario.isActivo() || usuario.isDebeActivar()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta todavía no está activa.");
        }
        String token = tokens.crear(usuario, TokenAcceso.TipoToken.RECUPERACION);
        if (correo.estaHabilitado()) {
            try {
                correo.enviar(usuario.getCorreo(), "Recuperación de contraseña Induztek",
                    "Ingresa este código en Induztek App para restablecer tu contraseña.\n\nCódigo de recuperación: "
                        + token + "\n\nEl código vence en 30 minutos y solo puede utilizarse una vez.");
                return new MensajeResponse("Se envió el código de recuperación al correo registrado.");
            } catch (RuntimeException ex) {
                log.error("No se pudo enviar el código de recuperación a {}.", usuario.getCorreo(), ex);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible enviar el correo de recuperación. Intenta nuevamente más tarde.");
            }
        }
        return new MensajeResponse("El envío de correo está deshabilitado. Contacta al administrador para habilitar la recuperación.");
    }

    @Transactional
    public void restablecer(RestablecerRequest request) {
        PasswordPolicy.exigir(request.nuevaContrasena());
        Usuario usuario = tokens.consumir(request.token(), TokenAcceso.TipoToken.RECUPERACION);
        usuario.setPasswordHash(encoder.encode(request.nuevaContrasena()));
        usuarios.save(usuario);
        correo.enviar(usuario.getCorreo(), "Contraseña actualizada - Induztek",
            "La contraseña de tu cuenta Induztek fue actualizada. Si no realizaste este cambio, contacta al administrador.");
    }

    @Transactional
    public void cambiarContrasena(String correoActual, CambiarContrasenaRequest request) {
        PasswordPolicy.exigir(request.nueva());
        Usuario usuario = obtenerPorCorreo(correoActual);
        if (!encoder.matches(request.actual(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no coincide.");
        }
        if (request.actual().equals(request.nueva())) throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual.");
        usuario.setPasswordHash(encoder.encode(request.nueva()));
        usuarios.save(usuario);
        correo.enviar(usuario.getCorreo(), "Contraseña actualizada - Induztek",
            "La contraseña de tu cuenta Induztek fue actualizada. Si no realizaste este cambio, contacta al administrador.");
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(String correoActual, ActualizarPerfilRequest request) {
        Usuario usuario = obtenerPorCorreo(correoActual);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(vacioANull(request.apellido()));
        usuario.setTelefono(request.telefono());
        boolean esCliente = usuario.getRol().getCodigo().equals("CLIENTE");
        usuario.setGiro(esCliente ? vacioANull(request.giro()) : null);
        usuario.setContactoNombre(esCliente ? vacioANull(request.contactoNombre()) : null);
        usuario.setContactoCargo(esCliente ? vacioANull(request.contactoCargo()) : null);
        return respuesta(usuarios.save(usuario));
    }

    public UsuarioResponse miPerfil(String correoActual) { return respuesta(obtenerPorCorreo(correoActual)); }

    @Transactional
    public UsuarioResponse actualizarAvatar(String correoActual, String avatarUrl) {
        Usuario usuario = obtenerPorCorreo(correoActual);
        usuario.setAvatarUrl(avatarUrl);
        return respuesta(usuarios.save(usuario));
    }

    public UsuarioResponse obtener(Long id, String correoActual) {
        Usuario actual = obtenerPorCorreo(correoActual);
        if (!actual.getId().equals(id) && !actual.getRol().getCodigo().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes consultar el perfil de otro usuario.");
        }
        return respuesta(usuarios.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.")));
    }

    public List<UsuarioResponse> listar() { return usuarios.findAll().stream().map(this::respuesta).toList(); }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarios.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        if (activo && usuario.isDebeActivar()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuenta debe activarse mediante su token y definir una contraseña.");
        }
        usuario.setActivo(activo);
        return respuesta(usuarios.save(usuario));
    }

    public UsuarioInternoResponse obtenerInterno(Long id) {
        Usuario usuario = usuarios.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        return new UsuarioInternoResponse(usuario.getId(), usuario.getRut(), usuario.getNombre(),
            usuario.getCorreo(), usuario.getRol().getCodigo(), usuario.isActivo() && !usuario.isDebeActivar());
    }

    public List<UsuarioInternoResponse> listarInternos(String rol) {
        return usuarios.findAll().stream()
            .filter(u -> rol == null || u.getRol().getCodigo().equals(rol))
            .map(u -> new UsuarioInternoResponse(u.getId(), u.getRut(), u.getNombre(), u.getCorreo(),
                u.getRol().getCodigo(), u.isActivo() && !u.isDebeActivar())).toList();
    }

    private Usuario obtenerPorCorreo(String correo) {
        return usuarios.findByCorreoIgnoreCase(correo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida."));
    }

    private UsuarioResponse respuesta(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getRut(), usuario.getNombre(), usuario.getApellido(),
            usuario.getCorreo(), usuario.getTelefono(), usuario.getRol().getCodigo(), usuario.isActivo(),
            usuario.getGiro(), usuario.getContactoNombre(), usuario.getContactoCargo(),
            usuario.getAvatarUrl(), usuario.getCreadoEn());
    }


    private static String normalizar(String valor) { return valor.trim().toLowerCase(Locale.ROOT); }
    private static String vacioANull(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private static String normalizarRut(String rut) {
        String valor = rut.replace(".", "").replace("-", "").trim().toUpperCase(Locale.ROOT);
        return valor.length() < 2 ? valor : valor.substring(0, valor.length() - 1) + "-" + valor.substring(valor.length() - 1);
    }
    private static boolean rutValido(String rut) {
        String valor = rut.replace("-", "");
        if (!valor.matches("[0-9]{7,8}[0-9K]")) return false;
        String cuerpo = valor.substring(0, valor.length() - 1);
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int resultado = 11 - suma % 11;
        char dv = resultado == 11 ? '0' : resultado == 10 ? 'K' : (char) ('0' + resultado);
        return dv == valor.charAt(valor.length() - 1);
    }
}
