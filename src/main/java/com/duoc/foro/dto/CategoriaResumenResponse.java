package com.duoc.foro.dto;

/** Resultado de la consulta agregada: cantidad de temas activos por categoría. */
public record CategoriaResumenResponse(Long id, String nombre, Long totalTemas) {
}
