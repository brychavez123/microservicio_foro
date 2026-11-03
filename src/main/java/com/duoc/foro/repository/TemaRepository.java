package com.duoc.foro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.duoc.foro.model.EstadoPublicacion;
import com.duoc.foro.model.Tema;

@Repository
public interface TemaRepository extends JpaRepository<Tema, Long> {

    List<Tema> findByEstadoOrderByFechaCreacionDesc(EstadoPublicacion estado);

    List<Tema> findByCategoriaIdAndEstadoOrderByFechaCreacionDesc(Long categoriaId, EstadoPublicacion estado);

    List<Tema> findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(Long usuarioId, EstadoPublicacion estado);

    boolean existsByCategoriaId(Long categoriaId);

    /** Búsqueda por texto en título o contenido, sin distinguir mayúsculas. */
    @Query("""
            select t from Tema t
            where t.estado = :estado
              and (lower(t.titulo) like lower(concat('%', :texto, '%'))
                   or lower(t.contenido) like lower(concat('%', :texto, '%')))
            order by t.fechaCreacion desc
            """)
    List<Tema> buscarPorTexto(@Param("texto") String texto, @Param("estado") EstadoPublicacion estado);
}
