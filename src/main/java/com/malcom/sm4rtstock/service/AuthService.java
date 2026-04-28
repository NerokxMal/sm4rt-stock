package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.exception.ConflictException;
import com.malcom.sm4rtstock.model.User;
import com.malcom.sm4rtstock.repository.UserRepository;
import com.malcom.sm4rtstock.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public String register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            // Antes: RuntimeException → devolvía 404 (incorrecto)
            // Ahora: ConflictException → devuelve 409 (correcto)
            throw new ConflictException("El nombre de usuario ya está en uso: " + username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();

        userRepository.save(user);
        return jwtTokenProvider.generateToken(username);
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        return jwtTokenProvider.generateToken(username);
    }
}
