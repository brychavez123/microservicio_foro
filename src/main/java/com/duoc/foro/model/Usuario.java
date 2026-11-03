package com.duoc.foro.model;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Tabla administrada por ms-usuarios; aquí solo se lee para autenticar
 * y para identificar al autor de temas y comentarios.
 */
@Entity
@Immutable
@Table(name = "USUARIO")
@Getter
@NoArgsConstructor
public class Usuario {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PASSWORD")
    private String password;

    @ManyToOne
    @JoinColumn(name = "ROL_ID")
    private Rol rol;
}
