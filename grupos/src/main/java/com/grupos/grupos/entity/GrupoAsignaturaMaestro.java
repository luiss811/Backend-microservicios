package com.grupos.grupos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Un maestro por materia en cada grupo. Si se necesitan varios, ampliar la restricción a las tres columnas.
@Entity
@Table(name = "grupo_asignatura_maestro",
        uniqueConstraints = @UniqueConstraint(columnNames = { "grupo_id", "asignatura_id" }))
@Getter
@Setter
public class GrupoAsignaturaMaestro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    // IDs en otros microservicios (sin FK entre microservicios).
    @Column(name = "asignatura_id", nullable = false)
    private Long asignaturaId;

    @Column(name = "maestro_id", nullable = false)
    private Long maestroId;
}
