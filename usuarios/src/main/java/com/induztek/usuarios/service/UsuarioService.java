package com.induztek.usuarios.service;

import com.induztek.usuarios.dto.ApiDtos.*;
import com.induztek.usuarios.model.*;
import com.induztek.usuarios.repository.UsuarioRepository;
import com.induztek.usuarios.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {
 private final UsuarioRepository repo; private final PasswordEncoder encoder; private final JwtService jwt;
 private final TokenService tokens; private final CorreoService correo;
 private final boolean exponerTokenDesarrollo;
 public UsuarioService(UsuarioRepository repo,PasswordEncoder encoder,JwtService jwt,TokenService tokens,
   CorreoService correo,@Value("${app.tokens.expose-in-development:false}") boolean expose){
  this.repo=repo;this.encoder=encoder;this.jwt=jwt;this.tokens=tokens;this.correo=correo;this.exponerTokenDesarrollo=expose;
 }
 @Transactional
 public MensajeResponse crear(CrearUsuarioRequest r){
  String mail=normalizar(r.correo());
  if(repo.existsByCorreoIgnoreCase(mail)) throw new ResponseStatusException(HttpStatus.CONFLICT,"El correo ya está registrado.");
  String rut=normalizarRut(r.rut());
  if(!rutValido(rut)) throw new IllegalArgumentException("El RUT no es válido.");
  if(repo.existsByRutIgnoreCase(rut)) throw new ResponseStatusException(HttpStatus.CONFLICT,"El RUT ya está registrado.");
  if(r.rol()==RolUsuario.CLIENTE &&
      (vacioANull(r.giro())==null || vacioANull(r.contactoNombre())==null || vacioANull(r.contactoCargo())==null))
    throw new IllegalArgumentException("Para una cuenta CLIENTE debes indicar giro, nombre de contacto y cargo.");
  Usuario u=new Usuario();u.setRut(rut);u.setNombre(r.nombre().trim());u.setApellido(vacioANull(r.apellido()));
  u.setCorreo(mail);u.setTelefono(r.telefono());u.setRol(r.rol());u.setActivo(false);u.setDebeActivar(true);
  u.setGiro(vacioANull(r.giro()));u.setContactoNombre(vacioANull(r.contactoNombre()));u.setContactoCargo(vacioANull(r.contactoCargo()));
  u=repo.save(u);
  String token=tokens.crear(u,TokenAcceso.TipoToken.INVITACION);
  String link=correo.enlace("activar",""+token);
  correo.enviar(mail,"Activa tu cuenta Induztek","Tu cuenta fue creada por un administrador. Define tu contraseña en el siguiente enlace (válido por 30 minutos):\n"+link);
  return new MensajeResponse(correo.estaHabilitado()?"Cuenta creada. Se envió un código de activación al correo.":"Cuenta creada. Activa la cuenta con el token de desarrollo.",(!correo.estaHabilitado()&&exponerTokenDesarrollo)?token:null);
 }
 @Transactional
 public UsuarioResponse activar(ActivarRequest r){
  PasswordPolicy.exigir(r.nuevaContrasena());Usuario u=tokens.consumir(r.token(),TokenAcceso.TipoToken.INVITACION);
  u.setPasswordHash(encoder.encode(r.nuevaContrasena()));u.setActivo(true);u.setDebeActivar(false);return respuesta(repo.save(u));
 }
 public AuthResponse login(LoginRequest r){
  Usuario u=repo.findByCorreoIgnoreCase(normalizar(r.correo())).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Correo o contraseña incorrectos."));
  if(!u.isActivo()||u.isDebeActivar()||u.getPasswordHash()==null||!encoder.matches(r.contrasena(),u.getPasswordHash()))
   throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Correo o contraseña incorrectos, o cuenta pendiente de activación.");
  return new AuthResponse(jwt.generar(u),"Bearer",respuesta(u));
 }
 @Transactional
 public MensajeResponse solicitarRecuperacion(RecuperarRequest r){
  Usuario u=repo.findByCorreoIgnoreCase(normalizar(r.correo())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"El correo no se encuentra registrado."));
  if(!u.isActivo()||u.isDebeActivar()) throw new ResponseStatusException(HttpStatus.CONFLICT,"La cuenta todavía no está activa.");
  String token=tokens.crear(u,TokenAcceso.TipoToken.RECUPERACION);
  String link=correo.enlace("restablecer",""+token);
  correo.enviar(u.getCorreo(),"Recuperación de contraseña Induztek","Usa este enlace para crear una nueva contraseña (válido por 30 minutos):\n"+link);
  return new MensajeResponse(correo.estaHabilitado()?"Se envió el código de recuperación al correo registrado.":"Correo validado. Usa el token de desarrollo para restablecer la contraseña.",(!correo.estaHabilitado()&&exponerTokenDesarrollo)?token:null);
 }
 @Transactional
 public void restablecer(RestablecerRequest r){
  PasswordPolicy.exigir(r.nuevaContrasena());Usuario u=tokens.consumir(r.token(),TokenAcceso.TipoToken.RECUPERACION);
  u.setPasswordHash(encoder.encode(r.nuevaContrasena()));repo.save(u);
 }
 @Transactional
 public void cambiarContrasena(String correoActual,CambiarContrasenaRequest r){
  PasswordPolicy.exigir(r.nueva());Usuario u=obtenerPorCorreo(correoActual);
  if(!encoder.matches(r.actual(),u.getPasswordHash())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La contraseña actual no coincide.");
  if(r.actual().equals(r.nueva())) throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual.");
  u.setPasswordHash(encoder.encode(r.nueva()));repo.save(u);
 }
 @Transactional
 public UsuarioResponse actualizarPerfil(String correoActual,ActualizarPerfilRequest r){
  Usuario u=obtenerPorCorreo(correoActual);
  if(u.getRol()==RolUsuario.CLIENTE &&
      (vacioANull(r.giro())==null || vacioANull(r.contactoNombre())==null || vacioANull(r.contactoCargo())==null))
    throw new IllegalArgumentException("Para una cuenta CLIENTE debes completar giro, nombre de contacto y cargo.");
  u.setNombre(r.nombre().trim());u.setApellido(vacioANull(r.apellido()));u.setTelefono(r.telefono());
  u.setGiro(vacioANull(r.giro()));u.setContactoNombre(vacioANull(r.contactoNombre()));u.setContactoCargo(vacioANull(r.contactoCargo()));
  return respuesta(repo.save(u));
 }
 public UsuarioResponse miPerfil(String correoActual){return respuesta(obtenerPorCorreo(correoActual));}
 @Transactional
 public UsuarioResponse actualizarAvatar(String correoActual,String avatarUrl){
  Usuario u=obtenerPorCorreo(correoActual);u.setAvatarUrl(avatarUrl);return respuesta(repo.save(u));
 }
 public UsuarioResponse obtener(Long id,String correoActual){
  Usuario actual=obtenerPorCorreo(correoActual);
  if(!actual.getId().equals(id)&&actual.getRol()!=RolUsuario.ADMIN) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No puedes consultar el perfil de otro usuario.");
  return respuesta(repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuario no encontrado.")));
 }
 public List<UsuarioResponse> listar(){return repo.findAll().stream().map(this::respuesta).toList();}
 private Usuario obtenerPorCorreo(String correo){return repo.findByCorreoIgnoreCase(correo).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Sesión no válida."));}
 private UsuarioResponse respuesta(Usuario u){return new UsuarioResponse(u.getId(),u.getRut(),u.getNombre(),u.getApellido(),u.getCorreo(),u.getTelefono(),u.getRol(),u.isActivo(),u.getGiro(),u.getContactoNombre(),u.getContactoCargo(),u.getAvatarUrl(),u.getCreadoEn());}
 private static String normalizar(String v){return v.trim().toLowerCase(Locale.ROOT);}
 private static String vacioANull(String v){return v==null||v.isBlank()?null:v.trim();}
 private static String normalizarRut(String r){String v=r.replace(".","").replace("-","").trim().toUpperCase(Locale.ROOT);return v.length()<2?v:v.substring(0,v.length()-1)+"-"+v.substring(v.length()-1);}
 private static boolean rutValido(String rut){
  String v=rut.replace("-","");if(!v.matches("[0-9]{7,8}[0-9K]"))return false;
  String cuerpo=v.substring(0,v.length()-1);int suma=0,f=2;
  for(int i=cuerpo.length()-1;i>=0;i--){suma+=(cuerpo.charAt(i)-'0')*f;f=f==7?2:f+1;}
  int res=11-suma%11;char dv=res==11?'0':res==10?'K':(char)('0'+res);
  return dv==v.charAt(v.length()-1);
 }
}
