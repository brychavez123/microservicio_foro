package com.duoc.foro.security;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.duoc.foro.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Roles:
 * - Público (sin login): ver y buscar categorías, temas y comentarios.
 * - USUARIO: publicar temas y comentar; editar/eliminar lo propio.
 * - MODERADOR: además banear/desbanear temas y comentarios y ver lo baneado.
 * - ADMIN: todo lo anterior + gestionar categorías y editar/eliminar cualquier publicación.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        // Moderación
                        .requestMatchers(HttpMethod.GET, "/api/temas/baneados", "/api/comentarios/baneados")
                        .hasAnyRole("ADMIN", "MODERADOR")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/temas/*/banear", "/api/temas/*/desbanear",
                                "/api/comentarios/*/banear", "/api/comentarios/*/desbanear")
                        .hasAnyRole("ADMIN", "MODERADOR")
                        // Gestión de categorías
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN")
                        // Lectura pública
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**", "/api/temas/**", "/api/comentarios/**")
                        .permitAll()
                        // Publicar, comentar, editar y eliminar requieren login
                        .anyRequest().authenticated())
                .httpBasic(basic -> basic.authenticationEntryPoint((req, res, ex) -> escribirError(mapper, req, res,
                        HttpStatus.UNAUTHORIZED, "Debes autenticarte con un email y contraseña válidos")))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> escribirError(mapper, req, res,
                                HttpStatus.UNAUTHORIZED, "Debes autenticarte con un email y contraseña válidos"))
                        .accessDeniedHandler((req, res, e) -> escribirError(mapper, req, res,
                                HttpStatus.FORBIDDEN, "Tu rol no tiene permisos para realizar esta acción")));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200", "http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static void escribirError(ObjectMapper mapper, HttpServletRequest req, HttpServletResponse res,
            HttpStatus status, String mensaje) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(),
                ErrorResponse.of(status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI()));
    }
}
