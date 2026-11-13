package com.induztek.usuarios.security;
import com.induztek.usuarios.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
 private final SecretKey key;
 private final long expiracionMinutos;
 public JwtService(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-minutes:60}") long minutos) {
   if(secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalStateException("app.jwt.secret debe tener al menos 32 bytes.");
   this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expiracionMinutos=minutos;
 }
 public String generar(Usuario usuario){
   Instant ahora=Instant.now();
   return Jwts.builder().subject(usuario.getCorreo()).claim("uid",usuario.getId())
     .claim("rol",usuario.getRol().name()).issuedAt(Date.from(ahora))
     .expiration(Date.from(ahora.plusSeconds(expiracionMinutos*60)))
     .signWith(key).compact();
 }
 public String obtenerCorreo(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
}
