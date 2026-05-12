package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.model.*;
import com.malcom.sm4rtstock.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;

    public DashboardStats obtenerStats() {
        int totalProductos = (int) productoRepository.count();
        int totalCategorias = (int) categoriaRepository.count();
        double valorInventario = Objects.requireNonNullElse(productoRepository.calcularValorInventario(), 0.0d);
        int stockBajo = (int) productoRepository.countStockCritico();

        List<Producto> productosStockBajo = productoRepository.findStockCritico(PageRequest.of(0, 5));
        productosStockBajo.sort(Comparator.comparingInt(Producto::getStock));

        List<DashboardStats.CategoriaStats> porCategoria = productoRepository.contarProductosPorCategoria().stream()
                .map(row -> new DashboardStats.CategoriaStats(
                        String.valueOf(row[0]),
                        ((Number) row[1]).intValue()
                ))
                .toList();

        LocalDateTime hace30Dias = LocalDateTime.now().minusDays(30);
        List<DashboardStats.MovimientoDia> movimientosPorDia = movimientoRepository.resumirPorDia(hace30Dias).stream()
                .map(row -> new DashboardStats.MovimientoDia(
                        String.valueOf(row[0]),
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).intValue()
                ))
                .toList();

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
