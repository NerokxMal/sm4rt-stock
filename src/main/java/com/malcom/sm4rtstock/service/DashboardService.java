package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.model.*;
import com.malcom.sm4rtstock.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;

    public DashboardStats obtenerStats() {

        List<Producto> productos = productoRepository.findAll();
        List<Categoria> categorias = categoriaRepository.findAll();

        // ─── Métricas simples ──────────────────────────────────────────
        int totalProductos   = productos.size();
        int totalCategorias  = categorias.size();

        double valorInventario = productos.stream()
                .mapToDouble(p -> p.getPrecio() * p.getStock())
                .sum();

        int stockBajo = (int) productos.stream()
                .filter(p -> p.getStock() < 5)
                .count();

        // ─── Tabla de stock crítico ────────────────────────────────────
        // Los 5 productos con menos stock, ordenados de menor a mayor.
        // Esta lista alimenta la tabla de alerta en el dashboard.
        List<Producto> productosStockBajo = productos.stream()
                .filter(p -> p.getStock() < 5)
                .sorted(Comparator.comparingInt(Producto::getStock))
                .limit(5)
                .collect(Collectors.toList());

        // ─── Distribución por categoría (donut) ───────────────────────
        // Agrupa los productos por nombre de categoría y cuenta cuántos hay en cada una.
        // Collectors.groupingBy + Collectors.counting hace el GROUP BY + COUNT en memoria.
        Map<String, Long> porCategoriaMap = productos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategoria() != null
                                ? p.getCategoria().getNombre()
                                : "Sin categoría",
                        Collectors.counting()
                ));

        // Convertimos el mapa a lista y ordenamos de mayor a menor para que
        // la categoría más grande aparezca primero en la leyenda de la donut.
        List<DashboardStats.CategoriaStats> porCategoria = porCategoriaMap.entrySet().stream()
                .map(e -> new DashboardStats.CategoriaStats(e.getKey(), e.getValue().intValue()))
                .sorted(Comparator.comparingInt(DashboardStats.CategoriaStats::cantidad).reversed())
                .collect(Collectors.toList());

        // ─── Movimientos por día (línea de tiempo) ────────────────────
        // Traemos todos los movimientos de los últimos 30 días.
        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        List<Movimiento> movimientos = movimientoRepository.findDesde(hace30Dias);

        // Formato dd/MM para las etiquetas del eje X de la gráfica
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        // TreeMap mantiene las claves ordenadas alfabéticamente/cronológicamente.
        // Como el formato es dd/MM, el orden lexicográfico coincide con el cronológico
        // dentro de un mismo mes. Para rangos que cruzan meses puede haber desfase —
        // si lo necesitas más preciso, cambia el formato a "yyyy-MM-dd".
        Map<String, int[]> porDia = new TreeMap<>();

        movimientos.forEach(m -> {
            String dia = m.getFecha().format(fmt);
            // computeIfAbsent crea el array [entradas, salidas] si el día no existe aún
            porDia.computeIfAbsent(dia, k -> new int[]{0, 0});
            if (m.getTipo() == TipoMovimiento.ENTRADA) {
                porDia.get(dia)[0] += m.getCantidad();  // índice 0 = entradas
            } else {
                porDia.get(dia)[1] += m.getCantidad();  // índice 1 = salidas
            }
        });

        List<DashboardStats.MovimientoDia> movimientosPorDia = porDia.entrySet().stream()
                .map(e -> new DashboardStats.MovimientoDia(
                        e.getKey(),
                        e.getValue()[0],  // entradas
                        e.getValue()[1]   // salidas
                ))
                .collect(Collectors.toList());

        // ─── Armar y devolver el record completo ───────────────────────
        return new DashboardStats(
                totalProductos,
                valorInventario,
                stockBajo,
                totalCategorias,
                porCategoria,
                movimientosPorDia,
                productosStockBajo
        );
    }
}