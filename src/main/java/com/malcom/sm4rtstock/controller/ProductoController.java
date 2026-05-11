package com.malcom.sm4rtstock.controller;

import com.malcom.sm4rtstock.model.Producto;
import com.malcom.sm4rtstock.service.ExportService;
import com.malcom.sm4rtstock.service.ProductoService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
//import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
//@Tag(name = "Productos", description = "Gestión de productos del inventario")
public class ProductoController {

    private final ProductoService productoService;
    private final ExportService exportService;

    @Data
    static class AjusteStockRequest {
        @NotNull(message = "La cantidad es obligatoria")
        private Integer cantidad;
        @NotBlank(message = "El motivo es obligatorio")
        private String motivo;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id,
                                               @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> porCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(productoService.buscarPorCategoria(categoria));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscar(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Producto>> stockBajo(@RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(productoService.stockBajo(limite));
    }

    @GetMapping("/stock-critico")
    public ResponseEntity<List<Producto>> stockCritico() {
        return ResponseEntity.ok(productoService.stockCritico());
    }

    @GetMapping("/stock-critico/count")
    public ResponseEntity<Long> stockCriticoCount() {
        return ResponseEntity.ok(productoService.contarStockCritico());
    }

    @PutMapping("/{id}/ajustar-stock")
    public ResponseEntity<Producto> ajustarStock(
            @PathVariable Long id,
            @Valid @RequestBody AjusteStockRequest request) {
        return ResponseEntity.ok(
                productoService.ajustarStock(id, request.getCantidad(), request.getMotivo())
        );
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('DATA_EXPORT')")
    public ResponseEntity<byte[]> exportar(@RequestParam(defaultValue = "csv") String format) {
        String normalized = format.toLowerCase(Locale.ROOT);
        byte[] payload = exportService.exportarProductos(normalized);
        MediaType mediaType = "pdf".equals(normalized)
                ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("text/csv");
        String extension = "pdf".equals(normalized) ? "pdf" : "csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"productos." + extension + "\"")
                .contentType(mediaType)
                .body(payload);
    }
}
