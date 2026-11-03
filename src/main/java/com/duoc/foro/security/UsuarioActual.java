package com.duoc.foro.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.duoc.foro.exception.AccesoDenegadoException;
import com.duoc.foro.exception.RecursoNoEncontradoException;
import com.duoc.foro.model.Usuario;
import com.duoc.foro.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/** Utilidades para conocer al usuario autenticado y sus privilegios. */
@Component
@RequiredArgsConstructor
public class UsuarioActual {

    private final UsuarioRepository usuarioRepository;

    public Usuario obtener(Authentication auth) {
        return usuarioRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));
    }

    public boolean esAdmin(Authentication auth) {
        return tieneRol(auth, "ADMIN");
    }

    /** ADMIN y MODERADOR pueden moderar (banear) y ver contenido baneado. */
    public boolean puedeModerar(Authentication auth) {
        return tieneRol(auth, "ADMIN") || tieneRol(auth, "MODERADOR");
    }

    /** Solo el autor o un ADMIN pueden editar/eliminar una publicación. */
    public void verificarAutorOAdmin(Usuario autor, Authentication auth, String mensaje) {
        if (esAdmin(auth)) {
            return;
        }
        if (auth == null || !autor.getEmail().equalsIgnoreCase(auth.getName())) {
            throw new AccesoDenegadoException(mensaje);
        }
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
}
