package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.exception.ConflictException;
import com.malcom.sm4rtstock.exception.ResourceNotFoundException;
import com.malcom.sm4rtstock.model.Categoria;
import com.malcom.sm4rtstock.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final AuditoriaService auditoriaService;

    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                // Antes: RuntimeException → siempre 404 (coincidencia correcta pero por accidente)
                // Ahora: ResourceNotFoundException → 404 de forma explícita e intencional
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + id));
    }

    @Transactional
    public Categoria crear(Categoria categoria) {
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            // Antes: RuntimeException → devolvía 404 (incorrecto, no es "no encontrado")
            // Ahora: ConflictException → devuelve 409 (correcto, hay un conflicto de datos)
            throw new ConflictException(
                    "Ya existe una categoría con el nombre: " + categoria.getNombre());
        }
        categoria.setParent(resolverParent(categoria, null));
        Categoria guardada = categoriaRepository.save(categoria);
        auditoriaService.registrar(
                "CREAR",
                "CATEGORIA",
                guardada.getId(),
                "Categoría creada: " + guardada.getNombre()
        );
        return guardada;
    }

    @Transactional
    public Categoria actualizar(Long id, Categoria categoria) {
        // obtenerPorId() ya lanza ResourceNotFoundException si no existe.
        // No necesitamos repetir la lógica aquí.
        Categoria existente = obtenerPorId(id);

        Optional<Categoria> categoriaConMismoNombre = categoriaRepository.findByNombre(categoria.getNombre());
        if (categoriaConMismoNombre.isPresent() && !categoriaConMismoNombre.get().getId().equals(id)) {
            throw new ConflictException("Ya existe una categoría con el nombre: " + categoria.getNombre());
        }

        existente.setNombre(categoria.getNombre());
        existente.setDescripcion(categoria.getDescripcion());
        existente.setParent(resolverParent(categoria, id));

        Categoria actualizada = categoriaRepository.save(existente);
        auditoriaService.registrar(
                "ACTUALIZAR",
                "CATEGORIA",
                actualizada.getId(),
                "Categoría actualizada: " + actualizada.getNombre()
        );
        return actualizada;
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtenerPorId(id); // Lanza 404 si no existe antes de intentar borrar
        if (categoria.getHijas() != null && !categoria.getHijas().isEmpty()) {
            throw new ConflictException("No se puede eliminar una categoría que tiene subcategorías");
        }
        categoriaRepository.deleteById(id);
        auditoriaService.registrar(
                "ELIMINAR",
                "CATEGORIA",
                id,
                "Categoría eliminada: " + categoria.getNombre()
        );
    }

    private Categoria resolverParent(Categoria categoria, Long categoriaActualId) {
        if (categoria.getParent() == null || categoria.getParent().getId() == null) {
            return null;
        }
        Long parentId = categoria.getParent().getId();
        if (categoriaActualId != null && categoriaActualId.equals(parentId)) {
            throw new ConflictException("Una categoría no puede ser su propia categoría padre");
        }
        Categoria parent = obtenerPorId(parentId);
        validarCiclos(categoriaActualId, parent);
        return parent;
    }

    private void validarCiclos(Long categoriaActualId, Categoria parent) {
        if (categoriaActualId == null) {
            return;
        }
        Categoria actual = parent;
        while (actual != null) {
            if (categoriaActualId.equals(actual.getId())) {
                throw new ConflictException("Jerarquía inválida: se detectó una referencia cíclica");
            }
            actual = actual.getParent();
        }
    }
}
