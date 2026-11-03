package com.duoc.foro.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.foro.dto.BaneoRequest;
import com.duoc.foro.dto.ComentarioRequest;
import com.duoc.foro.dto.ComentarioResponse;
import com.duoc.foro.dto.MensajeResponse;
import com.duoc.foro.service.ComentarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Los comentarios se crean y listan desde /api/temas/{id}/comentarios. */
@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioResponse> obtener(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(comentarioService.obtener(id, auth));
    }

    /** ADMIN / MODERADOR */
    @GetMapping("/baneados")
    public ResponseEntity<List<ComentarioResponse>> baneados() {
        return ResponseEntity.ok(comentarioService.listarBaneados());
    }

    /** Autor o ADMIN */
    @PutMapping("/{id}")
    public ResponseEntity<ComentarioResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody ComentarioRequest request, Authentication auth) {
        return ResponseEntity.ok(comentarioService.actualizar(id, request, auth));
    }

    /** Autor o ADMIN */
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id, Authentication auth) {
        comentarioService.eliminar(id, auth);
        return ResponseEntity.ok(new MensajeResponse("Comentario con id " + id + " eliminado correctamente"));
    }

    /** ADMIN / MODERADOR */
    @PutMapping("/{id}/banear")
    public ResponseEntity<ComentarioResponse> banear(@PathVariable Long id, @Valid @RequestBody BaneoRequest request,
            Authentication auth) {
        return ResponseEntity.ok(comentarioService.banear(id, request.motivo(), auth));
    }

    /** ADMIN / MODERADOR */
    @PutMapping("/{id}/desbanear")
    public ResponseEntity<ComentarioResponse> desbanear(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(comentarioService.desbanear(id, auth));
    }
}
