package com.induztek.usuarios.service;
import com.induztek.usuarios.model.*;
import com.induztek.usuarios.repository.TokenAccesoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class TokenService {
 private final TokenAccesoRepository repo; private final long minutos;
 public TokenService(TokenAccesoRepository repo,@Value("${app.tokens.expiration-minutes:30}") long minutos){this.repo=repo;this.minutos=minutos;}
 public String crear(Usuario usuario,TokenAcceso.TipoToken tipo){
   repo.deleteByUsuarioIdAndTipo(usuario.getId(),tipo);
   byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);
   String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
   TokenAcceso t=new TokenAcceso();t.setUsuario(usuario);t.setTipo(tipo);t.setTokenHash(hash(raw));t.setExpiraEn(LocalDateTime.now().plusMinutes(minutos));repo.save(t);return raw;
 }
 public Usuario consumir(String raw,TokenAcceso.TipoToken tipo){
   TokenAcceso t=repo.findByTokenHashAndTipoAndUsadoFalse(hash(raw),tipo)
     .orElseThrow(()->new IllegalArgumentException("El enlace no es válido o ya fue utilizado."));
   if(t.getExpiraEn().isBefore(LocalDateTime.now())) throw new IllegalArgumentException("El enlace ha expirado. Solicita uno nuevo.");
   t.setUsado(true);repo.save(t);return t.getUsuario();
 }
 private String hash(String raw){
   try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));}
   catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}
 }
}
