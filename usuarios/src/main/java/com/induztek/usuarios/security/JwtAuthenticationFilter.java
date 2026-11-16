package com.induztek.usuarios.security;

import com.induztek.usuarios.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UsuarioRepository usuarios;
    public JwtAuthenticationFilter(JwtService jwt, UsuarioRepository usuarios) {
        this.jwt = jwt;
        this.usuarios = usuarios;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
            && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String correo = jwt.obtenerCorreo(header.substring(7));
                usuarios.findByCorreoIgnoreCase(correo)
                    .filter(usuario -> usuario.isActivo() && !usuario.isDebeActivar())
                    .ifPresent(usuario -> {
                        var authorities = new ArrayList<SimpleGrantedAuthority>();
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getCodigo()));
                        usuario.getRol().getPermisos().forEach(p -> authorities.add(new SimpleGrantedAuthority("PERM_" + p.getCodigo())));
                        var authentication = new UsernamePasswordAuthenticationToken(usuario.getCorreo(), null, authorities);
                        authentication.setDetails(usuario.getId());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
