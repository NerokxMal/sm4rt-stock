package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.exception.ConflictException;
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
    private final AuditoriaService auditoriaService;

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
        normalizarUmbralCritico(producto);
        Producto guardado = productoRepository.save(producto);

        // Registrar movimiento inicial de stock si es mayor a 0
        if (guardado.getStock() > 0) {
            registrarMovimientoAuditoria(guardado, 0, guardado.getStock());
        }

        auditoriaService.registrar(
                "CREAR",
                "PRODUCTO",
                guardado.getId(),
                "Producto creado: " + guardado.getNombre()
        );

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
        Integer umbralActualizado = producto.getUmbralCritico() != null
                ? producto.getUmbralCritico()
                : (existente.getUmbralCritico() != null ? existente.getUmbralCritico() : 5);
        existente.setUmbralCritico(umbralActualizado);
        existente.setCategoria(producto.getCategoria());

        Producto guardado = productoRepository.save(existente);

        // Si el stock cambió, registramos el movimiento
        if (stockAnterior != guardado.getStock()) {
            registrarMovimientoAuditoria(guardado, stockAnterior, guardado.getStock());
        }

        auditoriaService.registrar(
                "ACTUALIZAR",
                "PRODUCTO",
                guardado.getId(),
                "Producto actualizado: " + guardado.getNombre()
        );

        return guardado;
    }

    // 5. Eliminar producto[cite: 12]
    @Transactional
    public void eliminar(Long id) {
        Producto producto = obtenerPorId(id);
        productoRepository.delete(producto);
        auditoriaService.registrar(
                "ELIMINAR",
                "PRODUCTO",
                id,
                "Producto eliminado: " + producto.getNombre()
        );
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
        if (limite == null) {
            return productoRepository.findStockCritico();
        }
        return productoRepository.findByStockLessThanEqual(limite);
    }

    public List<Producto> stockCritico() {
        return productoRepository.findStockCritico();
    }

    public long contarStockCritico() {
        return productoRepository.countStockCritico();
    }

    @Transactional
    public Producto ajustarStock(Long id, int cantidad, String motivo) {
        Producto producto = obtenerPorId(id);
        int stockAnterior = producto.getStock();
        int stockNuevo = stockAnterior + cantidad;

        if (stockNuevo < 0) {
            throw new ConflictException("El ajuste deja el stock en negativo para el producto: " + producto.getNombre());
        }

        producto.setStock(stockNuevo);
        Producto actualizado = productoRepository.save(producto);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User usuarioActual = null;
        if (auth != null && auth.getPrincipal() instanceof User u) {
            usuarioActual = u;
        }

        movimientoService.registrarAjuste(actualizado, usuarioActual, stockAnterior, stockNuevo, motivo);
        auditoriaService.registrar(
                "AJUSTAR_STOCK",
                "PRODUCTO",
                actualizado.getId(),
                "Ajuste manual de stock (" + cantidad + ") en producto: " + actualizado.getNombre()
        );

        return actualizado;
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

    private void normalizarUmbralCritico(Producto producto) {
        if (producto.getUmbralCritico() == null) {
            producto.setUmbralCritico(5);
        }
    }
}
