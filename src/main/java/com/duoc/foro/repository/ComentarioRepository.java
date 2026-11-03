package com.duoc.foro.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duoc.foro.model.Comentario;
import com.duoc.foro.model.EstadoPublicacion;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByTemaIdAndEstadoOrderByFechaAsc(Long temaId, EstadoPublicacion estado);

    List<Comentario> findByEstadoOrderByFechaDesc(EstadoPublicacion estado);

    long countByTemaIdAndEstado(Long temaId, EstadoPublicacion estado);

    void deleteByTemaId(Long temaId);
}
