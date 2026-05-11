package com.malcom.sm4rtstock.controller;

import com.malcom.sm4rtstock.model.Movimiento;
import com.malcom.sm4rtstock.model.TipoMovimiento;
import com.malcom.sm4rtstock.service.ExportService;
import com.malcom.sm4rtstock.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final ExportService exportService;

    // ─── HISTORIAL POR PRODUCTO (existente) ───────────────────────────
    @GetMapping("/producto/{id}")
    public ResponseEntity<List<Movimiento>> porProducto(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtenerPorProducto(id));
    }

    // ─── HISTORIAL GLOBAL CON FILTROS (nuevo) ─────────────────────────
    // Todos los parámetros son opcionales.
    // Si no se pasan, devuelve los últimos 30 días por defecto.
    //
    // Ejemplos de uso:
    //   GET /movimientos
    //   GET /movimientos?desde=2025-01-01&hasta=2025-01-31
    //   GET /movimientos?tipo=ENTRADA
    //   GET /movimientos?desde=2025-01-01&tipo=SALIDA
    @GetMapping
    public ResponseEntity<List<Movimiento>> historial(
            // @DateTimeFormat convierte el string "2025-01-15" al tipo LocalDate
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,

            @RequestParam(required = false) TipoMovimiento tipo
    ) {
        return ResponseEntity.ok(movimientoService.obtenerHistorial(desde, hasta, tipo));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('DATA_EXPORT')")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) TipoMovimiento tipo
    ) {
        String normalized = format.toLowerCase(Locale.ROOT);
        byte[] payload = exportService.exportarMovimientos(normalized, desde, hasta, tipo);
        MediaType mediaType = "pdf".equals(normalized)
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("text/csv");
        String extension = "pdf".equals(normalized) ? "pdf" : "csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"movimientos." + extension + "\"")
                .contentType(mediaType)
                .body(payload);
    }
}
