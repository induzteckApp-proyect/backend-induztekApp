package com.induztek.usuarios.config;
import com.induztek.usuarios.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationFilter jwt) throws Exception {
  return http.csrf(csrf->csrf.disable())
   .cors(Customizer.withDefaults())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a
     .requestMatchers("/api/auth/login","/api/auth/activar","/api/auth/recuperar","/api/auth/restablecer",
       "/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**","/actuator/health","/media/avatars/**").permitAll()
     .requestMatchers(HttpMethod.POST,"/api/usuarios").hasRole("ADMIN")
     .requestMatchers("/api/usuarios","/api/usuarios/**").authenticated()
     .anyRequest().authenticated())
   .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
 }
 @Bean CorsConfigurationSource corsConfigurationSource(){
  CorsConfiguration c=new CorsConfiguration(); c.setAllowedOriginPatterns(List.of("*"));
  c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
  c.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
  c.setExposedHeaders(List.of("Location")); c.setAllowCredentials(false);
  UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/**",c); return s;
 }
}
