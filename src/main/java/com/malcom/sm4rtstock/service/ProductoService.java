package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.exception.ResourceNotFoundException;
import com.malcom.sm4rtstock.model.Producto;
import com.malcom.sm4rtstock.model.User;
import com.malcom.sm4rtstock.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final MovimientoService movimientoService;

    // 1. Obtener todos los productos[cite: 12]
    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    // 2. Obtener por ID
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    // 3. Crear nuevo producto[cite: 12]
    @Transactional
    public Producto crear(Producto producto) {
        Producto guardado = productoRepository.save(producto);

        // Registrar movimiento inicial de stock si es mayor a 0
        if (guardado.getStock() > 0) {
            registrarMovimientoAuditoria(guardado, 0, guardado.getStock());
        }

        return guardado;
    }

    // 4. Actualizar producto existente[cite: 10, 12]
    @Transactional
    public Producto actualizar(Long id, Producto producto) {
        Producto existente = obtenerPorId(id);
        int stockAnterior = existente.getStock();

        existente.setNombre(producto.getNombre());
        existente.setDescripcion(producto.getDescripcion());
        existente.setPrecio(producto.getPrecio());
        existente.setStock(producto.getStock());
        existente.setCategoria(producto.getCategoria());

        Producto guardado = productoRepository.save(existente);

        // Si el stock cambió, registramos el movimiento
        if (stockAnterior != guardado.getStock()) {
            registrarMovimientoAuditoria(guardado, stockAnterior, guardado.getStock());
        }

        return guardado;
    }

    // 5. Eliminar producto[cite: 12]
    @Transactional
    public void eliminar(Long id) {
        Producto producto = obtenerPorId(id);
        productoRepository.delete(producto);
    }

    // 6. Buscar por nombre[cite: 12]
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // 7. Buscar por categoría[cite: 12]
    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaNombreIgnoreCase(categoria);
    }

    // 8. Alerta de stock bajo[cite: 12]
    public List<Producto> stockBajo(Integer limite) {
        return productoRepository.findByStockLessThanEqual(limite);
    }

    /**
     * Metodo privado para reutilizar la lógica de auditoría de movimientos
     */
    private void registrarMovimientoAuditoria(Producto producto, int anterior, int nuevo) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User usuarioActual = null;

        if (auth != null && auth.getPrincipal() instanceof User u) {
            usuarioActual = u;
        }

        movimientoService.registrar(producto, usuarioActual, anterior, nuevo);
    }
}