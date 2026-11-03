package com.duoc.foro.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.foro.dto.BaneoRequest;
import com.duoc.foro.dto.ComentarioRequest;
import com.duoc.foro.dto.ComentarioResponse;
import com.duoc.foro.dto.MensajeResponse;
import com.duoc.foro.dto.TemaRequest;
import com.duoc.foro.dto.TemaResponse;
import com.duoc.foro.service.ComentarioService;
import com.duoc.foro.service.TemaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/temas")
@RequiredArgsConstructor
public class TemaController {

    private final TemaService temaService;
    private final ComentarioService comentarioService;

    /** Público: temas activos, del más reciente al más antiguo. */
    @GetMapping
    public ResponseEntity<List<TemaResponse>> listar() {
        return ResponseEntity.ok(temaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemaResponse> obtener(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(temaService.obtener(id, auth));
    }

    /** Público: búsqueda por texto en título o contenido. */
    @GetMapping("/buscar")
    public ResponseEntity<List<TemaResponse>> buscar(@RequestParam(name = "q", required = false) String texto) {
        return ResponseEntity.ok(temaService.buscar(texto));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<TemaResponse>> porCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(temaService.listarPorCategoria(categoriaId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<TemaResponse>> porUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(temaService.listarPorUsuario(usuarioId));
    }

    /** ADMIN / MODERADOR */
    @GetMapping("/baneados")
    public ResponseEntity<List<TemaResponse>> baneados() {
        return ResponseEntity.ok(temaService.listarBaneados());
    }

    /** Usuario autenticado: el autor es quien inicia sesión. */
    @PostMapping
    public ResponseEntity<TemaResponse> crear(@Valid @RequestBody TemaRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(temaService.crear(request, auth));
    }

    /** Autor o ADMIN */
    @PutMapping("/{id}")
    public ResponseEntity<TemaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody TemaRequest request,
            Authentication auth) {
        return ResponseEntity.ok(temaService.actualizar(id, request, auth));
    }

    /** Autor o ADMIN */
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id, Authentication auth) {
        temaService.eliminar(id, auth);
        return ResponseEntity.ok(new MensajeResponse("Tema con id " + id + " eliminado correctamente"));
    }

    /** ADMIN / MODERADOR */
    @PutMapping("/{id}/banear")
    public ResponseEntity<TemaResponse> banear(@PathVariable Long id, @Valid @RequestBody BaneoRequest request,
            Authentication auth) {
        return ResponseEntity.ok(temaService.banear(id, request.motivo(), auth));
    }

    /** ADMIN / MODERADOR */
    @PutMapping("/{id}/desbanear")
    public ResponseEntity<TemaResponse> desbanear(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(temaService.desbanear(id, auth));
    }

    /** Público: comentarios activos de un tema. */
    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<ComentarioResponse>> comentarios(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(comentarioService.listarPorTema(id, auth));
    }

    /** Usuario autenticado: comentar un tema. */
    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> comentar(@PathVariable Long id,
            @Valid @RequestBody ComentarioRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioService.crear(id, request, auth));
    }
}
