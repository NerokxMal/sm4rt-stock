package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Al mover UserDetailsService a su propio @Service, rompemos el ciclo:
// JwtAuthFilter → UserDetailsServiceImpl (independiente)
// SecurityConfig → JwtAuthFilter (sin volver a UserDetailsService)
//
// Antes el ciclo era:
// JwtAuthFilter → SecurityConfig (para obtener UserDetailsService)
//               ↑                          ↓
//               └──────── SecurityConfig ──┘  (que necesitaba JwtAuthFilter)
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca el usuario en la BD. Si no existe, lanza UsernameNotFoundException
        // que Spring Security convierte en 401 Unauthorized automáticamente.
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));
    }
}
