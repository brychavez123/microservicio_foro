package com.duoc.foro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El autor y el tema no se envían: vienen del usuario autenticado y de la URL. */
public record ComentarioRequest(

        @NotBlank(message = "El contenido del comentario es obligatorio")
        @Size(min = 2, max = 1000, message = "El comentario debe tener entre 2 y 1000 caracteres")
        String contenido) {
}
