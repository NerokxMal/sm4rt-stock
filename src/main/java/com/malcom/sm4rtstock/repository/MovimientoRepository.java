package com.malcom.sm4rtstock.repository;

import com.malcom.sm4rtstock.model.Movimiento;
import com.malcom.sm4rtstock.model.TipoMovimiento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    // Historial de un producto, más reciente primero
    List<Movimiento> findByProductoIdOrderByFechaDesc(Long productoId);

    List<Movimiento> findByFechaBetweenOrderByFechaDesc(LocalDateTime desde, LocalDateTime hasta);

    List<Movimiento> findByFechaBetweenAndTipoOrderByFechaDesc(
            LocalDateTime desde,
            LocalDateTime hasta,
            TipoMovimiento tipo
    );

    // Para el dashboard: movimientos desde una fecha
    @Query("SELECT m FROM Movimiento m WHERE m.fecha >= :desde ORDER BY m.fecha ASC")
    List<Movimiento> findDesde(@Param("desde") LocalDateTime desde);

    // Para el histórico: filtros opcionales por rango de fechas y tipo.
    // El truco de JPQL para filtros opcionales es:
    //   (:param IS NULL OR m.campo = :param)
    // Si el parámetro llega como null, la condición siempre es true (se ignora).
    // Si llega con valor, filtra normalmente.
    // Esto evita tener que crear un método por cada combinación de filtros.
    @Query("""
        SELECT m FROM Movimiento m
        WHERE (:desde IS NULL OR m.fecha >= :desde)
          AND (:hasta IS NULL OR m.fecha <= :hasta)
          AND (:tipo  IS NULL OR m.tipo   = :tipo)
        ORDER BY m.fecha DESC
        """)
    List<Movimiento> findHistorial(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("tipo")  TipoMovimiento tipo
    );

    @Query("""
        SELECT m FROM Movimiento m
        LEFT JOIN FETCH m.producto p
        LEFT JOIN FETCH m.usuario u
        WHERE LOWER(COALESCE(p.nombre, '')) LIKE LOWER(CONCAT('%', :termino, '%'))
           OR LOWER(COALESCE(u.username, '')) LIKE LOWER(CONCAT('%', :termino, '%'))
           OR LOWER(COALESCE(m.motivo, '')) LIKE LOWER(CONCAT('%', :termino, '%'))
        ORDER BY m.fecha DESC
        """)
    List<Movimiento> buscarGlobal(@Param("termino") String termino, Pageable pageable);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', m.fecha, '%d/%m'),
               SUM(CASE
                       WHEN m.tipo = com.malcom.sm4rtstock.model.TipoMovimiento.ENTRADA
                            OR (m.tipo = com.malcom.sm4rtstock.model.TipoMovimiento.AJUSTE AND m.stockNuevo >= m.stockAnterior)
                       THEN m.cantidad
                       ELSE 0
                   END),
               SUM(CASE
                       WHEN m.tipo = com.malcom.sm4rtstock.model.TipoMovimiento.SALIDA
                            OR (m.tipo = com.malcom.sm4rtstock.model.TipoMovimiento.AJUSTE AND m.stockNuevo < m.stockAnterior)
                       THEN m.cantidad
                       ELSE 0
                   END)
        FROM Movimiento m
        WHERE m.fecha >= :desde
        GROUP BY FUNCTION('DATE', m.fecha), FUNCTION('DATE_FORMAT', m.fecha, '%d/%m')
        ORDER BY FUNCTION('DATE', m.fecha) ASC
        """)
    List<Object[]> resumirPorDia(@Param("desde") LocalDateTime desde);
}
