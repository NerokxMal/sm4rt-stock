package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.model.Movimiento;
import com.malcom.sm4rtstock.model.TipoMovimiento;
import com.malcom.sm4rtstock.model.User;
import com.malcom.sm4rtstock.model.Producto;
import com.malcom.sm4rtstock.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;

    // ─── REGISTRAR MOVIMIENTO ─────────────────────────────────────────
    @Transactional
    public void registrar(Producto producto, User usuario, int stockAnterior, int stockNuevo) {
        int diferencia = stockNuevo - stockAnterior;
        if (diferencia == 0) return;

        TipoMovimiento tipo = diferencia > 0 ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA;

        Movimiento m = Movimiento.builder()
                .producto(producto)
                .usuario(usuario)
                .tipo(tipo)
                .cantidad(Math.abs(diferencia))
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(null)
                .build();

        movimientoRepository.save(m);
    }

    @Transactional
    public void registrarAjuste(Producto producto, User usuario, int stockAnterior, int stockNuevo, String motivo) {
        int diferencia = stockNuevo - stockAnterior;
        if (diferencia == 0) return;

        Movimiento m = Movimiento.builder()
                .producto(producto)
                .usuario(usuario)
                .tipo(TipoMovimiento.AJUSTE)
                .cantidad(Math.abs(diferencia))
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(motivo)
                .build();

        movimientoRepository.save(m);
    }

    // ─── HISTORIAL POR PRODUCTO ───────────────────────────────────────
    public List<Movimiento> obtenerPorProducto(Long productoId) {
        return movimientoRepository.findByProductoIdOrderByFechaDesc(productoId);
    }

    // ─── HISTORIAL GLOBAL CON FILTROS ─────────────────────────────────
    // Convierte LocalDate (fecha sin hora) a LocalDateTime para la query.
    // "desde" arranca a las 00:00:00 del día indicado.
    // "hasta" termina a las 23:59:59 del día indicado — así incluimos
    // todos los movimientos de ese último día, no solo los de las 00:00.
    // Si no se pasa fecha, usa los últimos 30 días por defecto.
    public List<Movimiento> obtenerHistorial(LocalDate desde, LocalDate hasta, TipoMovimiento tipo) {
        LocalDateTime desdeDateTime = desde != null
                ? desde.atStartOfDay()
                : LocalDateTime.now().minusDays(30);

        LocalDateTime hastaDateTime = hasta != null
                ? hasta.atTime(23, 59, 59)
                : LocalDateTime.now();

        if (tipo == null) {
            return movimientoRepository.findByFechaBetweenOrderByFechaDesc(desdeDateTime, hastaDateTime);
        }
        return movimientoRepository.findByFechaBetweenAndTipoOrderByFechaDesc(desdeDateTime, hastaDateTime, tipo);
    }
}
