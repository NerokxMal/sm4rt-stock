package com.malcom.sm4rtstock.security;

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
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    // Inyectamos UserDetailsService para cargar el usuario completo desde la BD
    // a partir del username que viene dentro del token.
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // Cargamos el objeto UserDetails completo desde la BD.
                // Esto incluye el username, password y authorities (roles).
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // UsernamePasswordAuthenticationToken es el objeto que Spring Security
                // usa internamente para representar "este usuario está autenticado".
                // Parámetros: (usuario, credenciales, permisos)
                // Las credenciales son null porque con JWT no las necesitamos —
                // el token ya es la prueba de identidad.
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Adjuntamos detalles del request (IP, session) al objeto de autenticación.
                // No es obligatorio pero es buena práctica para auditoría.
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // *** ESTE ERA EL PASO QUE FALTABA ***
                // Le decimos a Spring Security: "este usuario está autenticado en este request".
                // Sin esta línea, Spring Security no sabe quién es el usuario
                // y bloquea el request con 403 aunque el token sea válido.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("No se puede establecer autenticación de usuario", e);
        }

        // Continuamos con la cadena de filtros independientemente del resultado.
        // Si el token era inválido, SecurityContextHolder queda vacío
        // y Spring Security bloqueará el request en el siguiente paso.
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // "Bearer " tiene 7 caracteres, recortamos toodo lo que viene después.
            return bearerToken.substring(7);
        }
        return null;
    }
}