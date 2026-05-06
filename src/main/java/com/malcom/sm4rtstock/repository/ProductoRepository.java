package com.malcom.sm4rtstock.repository;

import com.malcom.sm4rtstock.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos cuyo nombre contenga la cadena ignorando mayúsculas/minúsculas.[cite: 12, 13]
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca productos filtrando por el nombre de su categoría asociada.[cite: 12, 13]
     * El nombre del méwtodo debe seguir la ruta de la relación: Categoria -> Nombre.
     */
    List<Producto> findByCategoriaNombreIgnoreCase(String nombreCategoria);

    /**
     * Busca productos con un stock menor o igual al límite proporcionado.[cite: 12, 13]
     */
    List<Producto> findByStockLessThanEqual(Integer limite);
}