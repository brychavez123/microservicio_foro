package com.duoc.foro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.duoc.foro.dto.CategoriaResumenResponse;
import com.duoc.foro.model.Categoria;
import com.duoc.foro.model.EstadoPublicacion;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Categoria> findAllByOrderByNombreAsc();

    /** Cantidad de temas en un estado dado por cada categoría (incluye categorías sin temas). */
    @Query("""
            select new com.duoc.foro.dto.CategoriaResumenResponse(c.id, c.nombre, count(t.id))
            from Categoria c
            left join Tema t on t.categoria = c and t.estado = :estado
            group by c.id, c.nombre
            order by count(t.id) desc, c.nombre asc
            """)
    List<CategoriaResumenResponse> resumenTemasPorCategoria(@Param("estado") EstadoPublicacion estado);
}
