package com.malcom.sm4rtstock.repository;

import com.malcom.sm4rtstock.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaNombre(String nombre);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByStockLessThan(Integer stock);
}