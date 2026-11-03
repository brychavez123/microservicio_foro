package com.duoc.foro.dto;

import java.time.LocalDateTime;

import com.duoc.foro.model.Comentario;
import com.duoc.foro.model.EstadoPublicacion;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ComentarioResponse(
        Long id,
        String contenido,
        LocalDateTime fecha,
        EstadoPublicacion estado,
        String motivoBaneo,
        Long temaId,
        Long autorId,
        String autor) {

    public static ComentarioResponse from(Comentario comentario) {
        return new ComentarioResponse(
                comentario.getId(),
                comentario.getContenido(),
                comentario.getFecha(),
                comentario.getEstado(),
                comentario.getMotivoBaneo(),
                comentario.getTema().getId(),
                comentario.getUsuario().getId(),
                comentario.getUsuario().getNombre());
    }
}
