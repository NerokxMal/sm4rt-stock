package com.malcom.sm4rtstock.model;

import java.util.List;

// record = clase inmutable con getters automáticos, ideal para DTOs de respuesta.
// Jackson (la librería que convierte esto a JSON) lo serializa automáticamente.
// Cada campo del record se convierte en una propiedad del JSON de respuesta.
public record DashboardStats(

        // ──— Métricas simples (las 4 tarjetas del dashboard) ──────────
        int totalProductos,
        double valorInventario,
        int stockBajo,
        int totalCategorias,

        // ─── Datos para la gráfica donut ──────────────────────────────
        // Lista de cuántos productos tiene cada categoría
        List<CategoriaStats> porCategoria,

        // ─── Datos para la gráfica de línea ───────────────────────────
        // Entradas y salidas de stock agrupadas por día, últimos 30 días
        List<MovimientoDia> movimientosPorDia,

        // ─── Tabla de stock crítico ────────────────────────────────────
        // Los 5 productos con menos stock (stock < 5), ordenados de menor a mayor
        List<Producto> productosStockBajo

) {
    // Records anidados — representan las filas de las listas de arriba

    // Para la donut: nombre de categoría + cuántos productos tiene
    public record CategoriaStats(String nombre, int cantidad) {}

    // Para la línea: fecha del día + total de unidades que entraron y salieron
    public record MovimientoDia(String fecha, int entradas, int salidas) {}
}