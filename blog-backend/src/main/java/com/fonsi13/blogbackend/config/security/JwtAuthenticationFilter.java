package com.fonsi13.blogbackend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Obtener el header de autorización
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Validar que el header exista y empiece por "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Si no hay token, pasa al siguiente filtro (que rechazará la petición)
            return;
        }

        // Extraer el token (quitando la palabra "Bearer ")
        jwt = authHeader.substring(7);

        // Extraer el usuario del token
        username = jwtService.extractUsername(jwt);

        // 5. Si hay usuario y NO está autenticado todavía en el contexto...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargamos los detalles del usuario desde la DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 6. Validamos el token
            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                // Creamos la sesión de seguridad (Token de autenticación)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ESTABLECEMOS EL CONTEXTO: ¡Usuario Autenticado!
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}

