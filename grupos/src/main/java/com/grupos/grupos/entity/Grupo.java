package com.grupos.grupos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grupos")
@Getter
@Setter
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Boolean estado = true;

    // Tabla intermedia: materia + maestro que la imparte en este grupo.
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GrupoAsignaturaMaestro> asignaciones = new ArrayList<>();
}
