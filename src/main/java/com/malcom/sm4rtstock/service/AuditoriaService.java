package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.model.Auditoria;
import com.malcom.sm4rtstock.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    @Transactional
    public void registrar(String accion, String entidad, Long entidadId, String descripcion) {
        Auditoria registro = Auditoria.builder()
                .accion(accion)
                .entidad(entidad)
                .entidadId(entidadId)
                .descripcion(descripcion)
                .usuario(usuarioActual())
                .ip(ipActual())
                .build();
        auditoriaRepository.save(registro);
    }

    public List<Auditoria> listar(LocalDate desde, LocalDate hasta) {
        LocalDateTime desdeDateTime = (desde != null)
                ? desde.atStartOfDay()
                : LocalDateTime.now().minusDays(30);

        LocalDateTime hastaDateTime = (hasta != null)
                ? hasta.atTime(23, 59, 59)
                : LocalDateTime.now();

        return auditoriaRepository.findByFechaBetweenOrderByFechaDesc(desdeDateTime, hastaDateTime);
    }

    private String usuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "sistema";
        }
        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            return "sistema";
        }
        return username;
    }

    private String ipActual() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "N/A";
        }
        String forwarded = attrs.getRequest().getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return attrs.getRequest().getRemoteAddr();
    }
}
