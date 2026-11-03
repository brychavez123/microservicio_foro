package com.duoc.foro.dto;

import java.time.LocalDateTime;

import com.duoc.foro.model.EstadoPublicacion;
import com.duoc.foro.model.Tema;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TemaResponse(
        Long id,
        String titulo,
        String contenido,
        LocalDateTime fechaCreacion,
        EstadoPublicacion estado,
        String motivoBaneo,
        Long categoriaId,
        String categoria,
        Long autorId,
        String autor,
        long totalComentarios) {

    public static TemaResponse from(Tema tema, long totalComentarios) {
        return new TemaResponse(
                tema.getId(),
                tema.getTitulo(),
                tema.getContenido(),
                tema.getFechaCreacion(),
                tema.getEstado(),
                tema.getMotivoBaneo(),
                tema.getCategoria().getId(),
                tema.getCategoria().getNombre(),
                tema.getUsuario().getId(),
                tema.getUsuario().getNombre(),
                totalComentarios);
    }
}
