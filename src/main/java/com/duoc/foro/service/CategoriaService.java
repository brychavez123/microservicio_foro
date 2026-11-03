package com.duoc.foro.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.foro.dto.CategoriaRequest;
import com.duoc.foro.dto.CategoriaResponse;
import com.duoc.foro.dto.CategoriaResumenResponse;
import com.duoc.foro.exception.ConflictoException;
import com.duoc.foro.exception.RecursoNoEncontradoException;
import com.duoc.foro.model.Categoria;
import com.duoc.foro.model.EstadoPublicacion;
import com.duoc.foro.repository.CategoriaRepository;
import com.duoc.foro.repository.TemaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final TemaRepository temaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream().map(CategoriaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        return CategoriaResponse.from(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResumenResponse> resumen() {
        return categoriaRepository.resumenTemasPorCategoria(EstadoPublicacion.ACTIVO);
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.nombre().trim())) {
            throw new ConflictoException("Ya existe la categoría " + request.nombre());
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre().trim());
        categoria.setDescripcion(request.descripcion());
        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoría creada: id={}, nombre={}", guardada.getId(), guardada.getNombre());
        return CategoriaResponse.from(guardada);
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarPorId(id);
        String nuevoNombre = request.nombre().trim();
        if (!categoria.getNombre().equalsIgnoreCase(nuevoNombre)
                && categoriaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new ConflictoException("Ya existe la categoría " + nuevoNombre);
        }
        categoria.setNombre(nuevoNombre);
        categoria.setDescripcion(request.descripcion());
        log.info("Categoría actualizada: id={}", id);
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        if (temaRepository.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar la categoría '" + categoria.getNombre()
                    + "' porque tiene temas asociados");
        }
        categoriaRepository.delete(categoria);
        log.info("Categoría eliminada: id={}", id);
    }

    Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una categoría con id " + id));
    }
}
