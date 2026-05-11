package com.malcom.sm4rtstock.controller;

import com.malcom.sm4rtstock.model.BusquedaGlobalResponse;
import com.malcom.sm4rtstock.service.BusquedaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/buscar")
@RequiredArgsConstructor
public class BusquedaController {

    private final BusquedaService busquedaService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRODUCT_VIEW', 'CATEGORY_VIEW', 'HISTORY_VIEW')")
    public ResponseEntity<BusquedaGlobalResponse> buscar(
            @RequestParam String q,
            Authentication authentication
    ) {
        return ResponseEntity.ok(busquedaService.buscar(q, authentication));
    }
}
