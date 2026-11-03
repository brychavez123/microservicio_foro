package com.duoc.foro.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.foro.dto.TemaRequest;
import com.duoc.foro.dto.TemaResponse;
import com.duoc.foro.exception.ConflictoException;
import com.duoc.foro.exception.RecursoNoEncontradoException;
import com.duoc.foro.model.EstadoPublicacion;
import com.duoc.foro.model.Tema;
import com.duoc.foro.repository.ComentarioRepository;
import com.duoc.foro.repository.TemaRepository;
import com.duoc.foro.repository.UsuarioRepository;
import com.duoc.foro.security.UsuarioActual;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemaService {

    private final TemaRepository temaRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;
    private final UsuarioActual usuarioActual;

    @Transactional(readOnly = true)
    public List<TemaResponse> listar() {
        return aRespuestas(temaRepository.findByEstadoOrderByFechaCreacionDesc(EstadoPublicacion.ACTIVO));
    }

    /** Un tema baneado solo lo pueden ver ADMIN y MODERADOR. */
    @Transactional(readOnly = true)
    public TemaResponse obtener(Long id, Authentication auth) {
        return aRespuesta(buscarVisible(id, auth));
    }

    @Transactional(readOnly = true)
    public List<TemaResponse> listarPorCategoria(Long categoriaId) {
        categoriaService.buscarPorId(categoriaId);
        return aRespuestas(temaRepository.findByCategoriaIdAndEstadoOrderByFechaCreacionDesc(
                categoriaId, EstadoPublicacion.ACTIVO));
    }

    @Transactional(readOnly = true)
    public List<TemaResponse> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNoEncontradoException("No existe un usuario con id " + usuarioId);
        }
        return aRespuestas(temaRepository.findByUsuarioIdAndEstadoOrderByFechaCreacionDesc(
                usuarioId, EstadoPublicacion.ACTIVO));
    }

    @Transactional(readOnly = true)
    public List<TemaResponse> buscar(String texto) {
        if (texto == null || texto.isBlank()) {
            return listar();
        }
        return aRespuestas(temaRepository.buscarPorTexto(texto.trim(), EstadoPublicacion.ACTIVO));
    }

    @Transactional(readOnly = true)
    public List<TemaResponse> listarBaneados() {
        return aRespuestas(temaRepository.findByEstadoOrderByFechaCreacionDesc(EstadoPublicacion.BANEADO));
    }

    @Transactional
    public TemaResponse crear(TemaRequest request, Authentication auth) {
        Tema tema = new Tema();
        tema.setTitulo(request.titulo().trim());
        tema.setContenido(request.contenido().trim());
        tema.setCategoria(categoriaService.buscarPorId(request.categoriaId()));
        tema.setUsuario(usuarioActual.obtener(auth));

        Tema guardado = temaRepository.save(tema);
        log.info("Tema publicado: id={}, autor={}, categoria={}", guardado.getId(),
                guardado.getUsuario().getEmail(), guardado.getCategoria().getNombre());
        return aRespuesta(guardado);
    }

    @Transactional
    public TemaResponse actualizar(Long id, TemaRequest request, Authentication auth) {
        Tema tema = buscarPorId(id);
        usuarioActual.verificarAutorOAdmin(tema.getUsuario(), auth, "Solo el autor o un ADMIN pueden editar este tema");
        if (tema.getEstado() == EstadoPublicacion.BANEADO && !usuarioActual.esAdmin(auth)) {
            throw new ConflictoException("No se puede editar un tema baneado");
        }
        tema.setTitulo(request.titulo().trim());
        tema.setContenido(request.contenido().trim());
        tema.setCategoria(categoriaService.buscarPorId(request.categoriaId()));
        log.info("Tema actualizado: id={}", id);
        return aRespuesta(temaRepository.save(tema));
    }

    @Transactional
    public void eliminar(Long id, Authentication auth) {
        Tema tema = buscarPorId(id);
        usuarioActual.verificarAutorOAdmin(tema.getUsuario(), auth, "Solo el autor o un ADMIN pueden eliminar este tema");
        comentarioRepository.deleteByTemaId(id);
        temaRepository.delete(tema);
        log.info("Tema eliminado: id={} (junto a sus comentarios)", id);
    }

    @Transactional
    public TemaResponse banear(Long id, String motivo, Authentication auth) {
        Tema tema = buscarPorId(id);
        if (tema.getEstado() == EstadoPublicacion.BANEADO) {
            throw new ConflictoException("El tema con id " + id + " ya está baneado");
        }
        tema.setEstado(EstadoPublicacion.BANEADO);
        tema.setMotivoBaneo(motivo.trim());
        log.warn("Tema baneado: id={}, por={}, motivo={}", id, auth.getName(), motivo);
        return aRespuesta(temaRepository.save(tema));
    }

    @Transactional
    public TemaResponse desbanear(Long id, Authentication auth) {
        Tema tema = buscarPorId(id);
        if (tema.getEstado() == EstadoPublicacion.ACTIVO) {
            throw new ConflictoException("El tema con id " + id + " no está baneado");
        }
        tema.setEstado(EstadoPublicacion.ACTIVO);
        tema.setMotivoBaneo(null);
        log.info("Tema desbaneado: id={}, por={}", id, auth.getName());
        return aRespuesta(temaRepository.save(tema));
    }

    Tema buscarPorId(Long id) {
        return temaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un tema con id " + id));
    }

    Tema buscarVisible(Long id, Authentication auth) {
        Tema tema = buscarPorId(id);
        if (tema.getEstado() == EstadoPublicacion.BANEADO && !usuarioActual.puedeModerar(auth)) {
            throw new RecursoNoEncontradoException("No existe un tema con id " + id);
        }
        return tema;
    }

    private TemaResponse aRespuesta(Tema tema) {
        return TemaResponse.from(tema, comentarioRepository.countByTemaIdAndEstado(tema.getId(), EstadoPublicacion.ACTIVO));
    }

    private List<TemaResponse> aRespuestas(List<Tema> temas) {
        return temas.stream().map(this::aRespuesta).toList();
    }
}
