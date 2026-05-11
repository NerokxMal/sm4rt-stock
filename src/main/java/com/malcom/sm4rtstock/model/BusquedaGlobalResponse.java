package com.malcom.sm4rtstock.model;

import java.time.LocalDateTime;
import java.util.List;

public record BusquedaGlobalResponse(
        List<Producto> productos,
        List<Categoria> categorias,
        List<MovimientoItem> movimientos
) {
    public record MovimientoItem(
            Long id,
            String producto,
            String usuario,
            TipoMovimiento tipo,
            Integer cantidad,
            String motivo,
            LocalDateTime fecha
    ) {}
}
