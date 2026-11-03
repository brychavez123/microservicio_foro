package com.duoc.foro.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.foro.dto.ComentarioRequest;
import com.duoc.foro.dto.ComentarioResponse;
import com.duoc.foro.exception.ConflictoException;
import com.duoc.foro.exception.RecursoNoEncontradoException;
import com.duoc.foro.model.Comentario;
import com.duoc.foro.model.EstadoPublicacion;
import com.duoc.foro.model.Tema;
import com.duoc.foro.repository.ComentarioRepository;
import com.duoc.foro.security.UsuarioActual;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final TemaService temaService;
    private final UsuarioActual usuarioActual;

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorTema(Long temaId, Authentication auth) {
        temaService.buscarVisible(temaId, auth);
        return comentarioRepository.findByTemaIdAndEstadoOrderByFechaAsc(temaId, EstadoPublicacion.ACTIVO).stream()
                .map(ComentarioResponse::from).toList();
    }

    /** Un comentario baneado solo lo pueden ver ADMIN y MODERADOR. */
    @Transactional(readOnly = true)
    public ComentarioResponse obtener(Long id, Authentication auth) {
        Comentario comentario = buscarPorId(id);
        if (comentario.getEstado() == EstadoPublicacion.BANEADO && !usuarioActual.puedeModerar(auth)) {
            throw new RecursoNoEncontradoException("No existe un comentario con id " + id);
        }
        temaService.buscarVisible(comentario.getTema().getId(), auth);
        return ComentarioResponse.from(comentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarBaneados() {
        return comentarioRepository.findByEstadoOrderByFechaDesc(EstadoPublicacion.BANEADO).stream()
                .map(ComentarioResponse::from).toList();
    }

    @Transactional
    public ComentarioResponse crear(Long temaId, ComentarioRequest request, Authentication auth) {
        Tema tema = temaService.buscarVisible(temaId, auth);
        if (tema.getEstado() == EstadoPublicacion.BANEADO) {
            throw new ConflictoException("No se puede comentar un tema baneado");
        }
        Comentario comentario = new Comentario();
        comentario.setContenido(request.contenido().trim());
        comentario.setTema(tema);
        comentario.setUsuario(usuarioActual.obtener(auth));

        Comentario guardado = comentarioRepository.save(comentario);
        log.info("Comentario publicado: id={}, tema={}, autor={}", guardado.getId(), temaId, auth.getName());
        return ComentarioResponse.from(guardado);
    }

    @Transactional
    public ComentarioResponse actualizar(Long id, ComentarioRequest request, Authentication auth) {
        Comentario comentario = buscarPorId(id);
        usuarioActual.verificarAutorOAdmin(comentario.getUsuario(), auth,
                "Solo el autor o un ADMIN pueden editar este comentario");
        if (comentario.getEstado() == EstadoPublicacion.BANEADO && !usuarioActual.esAdmin(auth)) {
            throw new ConflictoException("No se puede editar un comentario baneado");
        }
        comentario.setContenido(request.contenido().trim());
        log.info("Comentario actualizado: id={}", id);
        return ComentarioResponse.from(comentarioRepository.save(comentario));
    }

    @Transactional
    public void eliminar(Long id, Authentication auth) {
        Comentario comentario = buscarPorId(id);
        usuarioActual.verificarAutorOAdmin(comentario.getUsuario(), auth,
                "Solo el autor o un ADMIN pueden eliminar este comentario");
        comentarioRepository.delete(comentario);
        log.info("Comentario eliminado: id={}", id);
    }

    @Transactional
    public ComentarioResponse banear(Long id, String motivo, Authentication auth) {
        Comentario comentario = buscarPorId(id);
        if (comentario.getEstado() == EstadoPublicacion.BANEADO) {
            throw new ConflictoException("El comentario con id " + id + " ya está baneado");
        }
        comentario.setEstado(EstadoPublicacion.BANEADO);
        comentario.setMotivoBaneo(motivo.trim());
        log.warn("Comentario baneado: id={}, por={}, motivo={}", id, auth.getName(), motivo);
        return ComentarioResponse.from(comentarioRepository.save(comentario));
    }

    @Transactional
    public ComentarioResponse desbanear(Long id, Authentication auth) {
        Comentario comentario = buscarPorId(id);
        if (comentario.getEstado() == EstadoPublicacion.ACTIVO) {
            throw new ConflictoException("El comentario con id " + id + " no está baneado");
        }
        comentario.setEstado(EstadoPublicacion.ACTIVO);
        comentario.setMotivoBaneo(null);
        log.info("Comentario desbaneado: id={}, por={}", id, auth.getName());
        return ComentarioResponse.from(comentarioRepository.save(comentario));
    }

    private Comentario buscarPorId(Long id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un comentario con id " + id));
    }
}
