package com.microservicio.alumnos.dto;

import lombok.Data;

@Data
public class dto {
    private Long id;
    private String nombreAlumno;
    private String matricula;
    private Boolean estadoAlumno;
    private Long entityAsignaturaId;

    public dto() {
    }

    public dto(Long id, String nombreAlumno, String matricula, Boolean estadoAlumno, Long entityAsignaturaId) {
        this.id = id;
        this.nombreAlumno = nombreAlumno;
        this.matricula = matricula;
        this.estadoAlumno = estadoAlumno;
        this.entityAsignaturaId = entityAsignaturaId;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreAlumno() {
        return this.nombreAlumno;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public String getMatricula() {
        return this.matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Boolean getEstadoAlumno() {
        return this.estadoAlumno;
    }

    public void setEstadoAlumno(Boolean estadoAlumno) {
        this.estadoAlumno = estadoAlumno;
    }

    public Long getEntityAsignaturaId() {
        return this.entityAsignaturaId;
    }

    public void setEntityAsignaturaId(Long entityAsignaturaId) {
        this.entityAsignaturaId = entityAsignaturaId;
    }

    // Métodos de compatibilidad y conveniencia
    public String getNombre() {
        return this.nombreAlumno;
    }

    public void setNombre(String nombre) {
        if (this.nombreAlumno == null) {
            this.nombreAlumno = nombre;
        }
    }

    public Boolean getEstado() {
        return this.estadoAlumno;
    }

    public void setEstado(Boolean estado) {
        if (this.estadoAlumno == null) {
            this.estadoAlumno = estado;
        }
    }

    public Long getAsignaturaId() {
        return this.entityAsignaturaId;
    }

    public void setAsignaturaId(Long asignaturaId) {
        if (this.entityAsignaturaId == null) {
            this.entityAsignaturaId = asignaturaId;
        }
    }
}
