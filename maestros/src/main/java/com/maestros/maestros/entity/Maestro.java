package com.maestros.maestros.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "maestros")
@Getter
@Setter
public class Maestro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 120)
    private String correo;

    @Column(nullable = false)
    private Boolean estado = true;

    // Tabla intermedia: materias que imparte el maestro.
    @OneToMany(mappedBy = "maestro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaestroAsignatura> asignaturas = new ArrayList<>();
}
