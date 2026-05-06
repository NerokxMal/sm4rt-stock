package com.malcom.sm4rtstock.repository;

import com.malcom.sm4rtstock.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByProductoIdOrderByFechaDesc(Long productoId);

    // @Param("desde") vincula el parámetro :desde del JPQL con el argumento del metodo
    // Sin @Param IntelliJ no puede resolver la referencia y Spring falla en runtime
    @Query("SELECT m FROM Movimiento m WHERE m.fecha >= :desde ORDER BY m.fecha ASC")
    List<Movimiento> findDesde(@Param("desde") LocalDateTime desde);
}