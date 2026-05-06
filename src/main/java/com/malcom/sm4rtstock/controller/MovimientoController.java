package com.malcom.sm4rtstock.controller;

import com.malcom.sm4rtstock.model.Movimiento;
import com.malcom.sm4rtstock.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @GetMapping("/producto/{id}")
    // Este endpoint es público — cualquiera puede ver el historial
    // Si quisieras restringirlo, agrega @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Movimiento>> porProducto(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtenerPorProducto(id));
    }
}