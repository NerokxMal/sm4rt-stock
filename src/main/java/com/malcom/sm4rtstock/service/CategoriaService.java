package com.malcom.sm4rtstock.service;

import com.malcom.sm4rtstock.exception.ConflictException;
import com.malcom.sm4rtstock.exception.ResourceNotFoundException;
import com.malcom.sm4rtstock.model.Categoria;
import com.malcom.sm4rtstock.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

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

    public Categoria crear(Categoria categoria) {
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            // Antes: RuntimeException → devolvía 404 (incorrecto, no es "no encontrado")
            // Ahora: ConflictException → devuelve 409 (correcto, hay un conflicto de datos)
            throw new ConflictException(
                    "Ya existe una categoría con el nombre: " + categoria.getNombre());
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria categoria) {
        // obtenerPorId() ya lanza ResourceNotFoundException si no existe.
        // No necesitamos repetir la lógica aquí.
        Categoria existente = obtenerPorId(id);
        existente.setNombre(categoria.getNombre());
        existente.setDescripcion(categoria.getDescripcion());
        return categoriaRepository.save(existente);
    }

    public void eliminar(Long id) {
        obtenerPorId(id); // Lanza 404 si no existe antes de intentar borrar
        categoriaRepository.deleteById(id);
    }
}
