package com.duoc.foro.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.foro.dto.CategoriaRequest;
import com.duoc.foro.dto.CategoriaResponse;
import com.duoc.foro.dto.CategoriaResumenResponse;
import com.duoc.foro.dto.MensajeResponse;
import com.duoc.foro.dto.TemaResponse;
import com.duoc.foro.service.CategoriaService;
import com.duoc.foro.service.TemaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final TemaService temaService;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtener(id));
    }

    /** Cantidad de temas activos por categoría. */
    @GetMapping("/resumen")
    public ResponseEntity<List<CategoriaResumenResponse>> resumen() {
        return ResponseEntity.ok(categoriaService.resumen());
    }

    /** Temas activos de una categoría. */
    @GetMapping("/{id}/temas")
    public ResponseEntity<List<TemaResponse>> temas(@PathVariable Long id) {
        return ResponseEntity.ok(temaService.listarPorCategoria(id));
    }

    /** ADMIN */
    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(request));
    }

    /** ADMIN */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    /** ADMIN */
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.ok(new MensajeResponse("Categoría con id " + id + " eliminada correctamente"));
    }
}
