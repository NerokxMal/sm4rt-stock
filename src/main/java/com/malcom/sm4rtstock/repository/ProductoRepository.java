package com.malcom.sm4rtstock.repository;

import com.malcom.sm4rtstock.model.Producto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca productos cuyo nombre contenga la cadena ignorando mayúsculas/minúsculas.[cite: 12, 13]
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    /**
     * Busca productos filtrando por el nombre de su categoría asociada.[cite: 12, 13]
     * El nombre del méwtodo debe seguir la ruta de la relación: Categoria -> Nombre.
     */
    List<Producto> findByCategoriaNombreIgnoreCase(String nombreCategoria);

    /**
     * Busca productos con un stock menor o igual al límite proporcionado.[cite: 12, 13]
     */
    List<Producto> findByStockLessThanEqual(Integer limite);

    @Query("SELECT p FROM Producto p WHERE p.stock <= COALESCE(p.umbralCritico, 5) ORDER BY p.stock ASC")
    List<Producto> findStockCritico();

    @Query("SELECT p FROM Producto p WHERE p.stock <= COALESCE(p.umbralCritico, 5) ORDER BY p.stock ASC")
    List<Producto> findStockCritico(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stock <= COALESCE(p.umbralCritico, 5)")
    long countStockCritico();

    @Query("SELECT COALESCE(SUM(p.precio * p.stock), 0) FROM Producto p")
    Double calcularValorInventario();

    @Query("""
        SELECT COALESCE(c.nombre, 'Sin categoría'), COUNT(p)
        FROM Producto p
        LEFT JOIN p.categoria c
        GROUP BY COALESCE(c.nombre, 'Sin categoría')
        ORDER BY COUNT(p) DESC
        """)
    List<Object[]> contarProductosPorCategoria();
}
