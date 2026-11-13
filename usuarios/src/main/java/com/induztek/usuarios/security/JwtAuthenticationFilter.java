package com.induztek.usuarios.security;
import com.induztek.usuarios.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 private final JwtService jwt; private final UsuarioRepository usuarios;
 public JwtAuthenticationFilter(JwtService jwt, UsuarioRepository usuarios){this.jwt=jwt;this.usuarios=usuarios;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
   String header=req.getHeader("Authorization");
   if(header!=null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication()==null){
     try {
       String correo=jwt.obtenerCorreo(header.substring(7));
       usuarios.findByCorreoIgnoreCase(correo).filter(u->u.isActivo() && !u.isDebeActivar()).ifPresent(u->{
         var auth=new UsernamePasswordAuthenticationToken(u.getCorreo(),null,List.of(new SimpleGrantedAuthority("ROLE_"+u.getRol().name())));
         SecurityContextHolder.getContext().setAuthentication(auth);
       });
     } catch(Exception ignored) { /* token inválido: continúa sin autenticar y Security responde 401 */ }
   }
   chain.doFilter(req,res);
 }
}
