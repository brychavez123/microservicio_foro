package com.duoc.foro.model;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Tabla administrada por ms-usuarios; aquí solo se lee. */
@Entity
@Immutable
@Table(name = "ROL")
@Getter
@NoArgsConstructor
public class Rol {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "NOMBRE")
    private String nombre;
}
