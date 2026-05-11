package com.malcom.sm4rtstock;

import com.malcom.sm4rtstock.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    // Ya no inyectamos UserRepository aquí — UserDetailsServiceImpl
    // lo maneja de forma independiente y Spring lo encuentra automáticamente
    // gracias a que implementa la interfaz UserDetailsService con @Service.

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // El Bean userDetailsService() fue eliminado de aquí.
    // Spring detecta automáticamente UserDetailsServiceImpl porque:
    // 1. Tiene @Service
    // 2. Implementa UserDetailsService
    // No necesita estar declarado explícitamente en SecurityConfig.

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:[*]",
                "http://127.0.0.1:[*]",
                "https://sm4rt-stock.netlify.app",
                "https://*.netlify.app",
                "https://*.vercel.app"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/auth/password").authenticated()
                        .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/auth/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/auth/permissions").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/auth/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/auth/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/productos/**").hasAuthority("PRODUCT_VIEW")
                        .requestMatchers(HttpMethod.POST, "/productos/**").hasAuthority("PRODUCT_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/productos/**").hasAuthority("PRODUCT_STOCK_EDIT")
                        .requestMatchers(HttpMethod.DELETE, "/productos/**").hasAuthority("PRODUCT_DELETE")
                        .requestMatchers(HttpMethod.GET, "/categorias/**").hasAuthority("CATEGORY_VIEW")
                        .requestMatchers(HttpMethod.POST, "/categorias/**").hasAuthority("CATEGORY_MANAGE")
                        .requestMatchers(HttpMethod.PUT, "/categorias/**").hasAuthority("CATEGORY_MANAGE")
                        .requestMatchers(HttpMethod.DELETE, "/categorias/**").hasAuthority("CATEGORY_MANAGE")
                        .requestMatchers(HttpMethod.GET, "/movimientos/**").hasAuthority("HISTORY_VIEW")
                        .requestMatchers(HttpMethod.GET, "/dashboard/**").hasAuthority("DASHBOARD_VIEW")
                        .requestMatchers(HttpMethod.GET, "/auditoria/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
