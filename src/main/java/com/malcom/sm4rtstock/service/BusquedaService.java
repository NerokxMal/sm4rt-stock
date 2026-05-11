package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.model.BusquedaGlobalResponse;
import com.malcom.sm4rtstock.model.Categoria;
import com.malcom.sm4rtstock.model.Movimiento;
import com.malcom.sm4rtstock.model.Producto;
import com.malcom.sm4rtstock.repository.CategoriaRepository;
import com.malcom.sm4rtstock.repository.MovimientoRepository;
import com.malcom.sm4rtstock.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusquedaService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;

    public BusquedaGlobalResponse buscar(String q, Authentication authentication) {
        String termino = q == null ? "" : q.trim();
        if (termino.isEmpty()) {
            return new BusquedaGlobalResponse(List.of(), List.of(), List.of());
        }

        Set<String> authorities = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toSet());

        Pageable limit10 = PageRequest.of(0, 10);

        List<Producto> productos = authorities.contains("PRODUCT_VIEW")
                ? productoRepository.findByNombreContainingIgnoreCase(termino, limit10)
                : List.of();

        List<Categoria> categorias = authorities.contains("CATEGORY_VIEW")
                ? categoriaRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                termino, termino, limit10)
                : List.of();

        List<BusquedaGlobalResponse.MovimientoItem> movimientos = authorities.contains("HISTORY_VIEW")
                ? mapearMovimientos(movimientoRepository.buscarGlobal(termino, limit10))
                : List.of();

        return new BusquedaGlobalResponse(productos, categorias, movimientos);
    }

    private List<BusquedaGlobalResponse.MovimientoItem> mapearMovimientos(List<Movimiento> movimientos) {
        return movimientos.stream()
                .map(m -> new BusquedaGlobalResponse.MovimientoItem(
                        m.getId(),
                        m.getProducto() != null ? m.getProducto().getNombre() : "N/A",
                        m.getUsuario() != null ? m.getUsuario().getUsername() : "sistema",
                        m.getTipo(),
                        m.getCantidad(),
                        m.getMotivo(),
                        m.getFecha()
                ))
                .toList();
    }
}
