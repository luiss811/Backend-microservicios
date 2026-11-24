package com.maestros.maestros.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "maestro_asignatura",
        uniqueConstraints = @UniqueConstraint(columnNames = { "maestro_id", "asignatura_id" }))
@Getter
@Setter
public class MaestroAsignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maestro_id")
    private Maestro maestro;

    // ID en el microservicio de Asignaturas (sin FK entre microservicios).
    @Column(name = "asignatura_id", nullable = false)
    private Long asignaturaId;
}
