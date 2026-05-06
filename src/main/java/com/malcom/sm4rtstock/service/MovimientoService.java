package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.model.*;
import com.malcom.sm4rtstock.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;

    @Transactional
    public void registrar(Producto producto, User usuario, int stockAnterior, int stockNuevo) {
        int diferencia = stockNuevo - stockAnterior;
        if (diferencia == 0) return; // No registrar si no hubo cambios reales[cite: 13]

        TipoMovimiento tipo = diferencia > 0 ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA;

        Movimiento m = Movimiento.builder()
                .producto(producto)
                .usuario(usuario)
                .tipo(tipo)
                .cantidad(Math.abs(diferencia))
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .build();

        movimientoRepository.save(m);
    }

    public List<Movimiento> obtenerPorProducto(Long productoId) {
        // Ordenado por fecha descendente para ver lo más reciente arriba[cite: 12, 13]
        return movimientoRepository.findByProductoIdOrderByFechaDesc(productoId);
    }
}