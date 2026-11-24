package com.microservicio.alumnos.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "alumno_asignatura")
@Data
public class EntityAlumnoAsignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asignatura_id", nullable = false)
    private Long entityAsignaturaId;

    public EntityAlumnoAsignatura() {
    }

    public EntityAlumnoAsignatura(Long entityAsignaturaId) {
        this.entityAsignaturaId = entityAsignaturaId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEntityAsignaturaId() {
        return entityAsignaturaId;
    }

    public void setEntityAsignaturaId(Long entityAsignaturaId) {
        this.entityAsignaturaId = entityAsignaturaId;
    }

    // Métodos de conveniencia
    public Long getAsignaturaId() {
        return this.entityAsignaturaId;
    }

    public void setAsignaturaId(Long asignaturaId) {
        this.entityAsignaturaId = asignaturaId;
    }
}
