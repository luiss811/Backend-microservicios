package com.microservicio.alumnos.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alumnos")
@Data
public class EntityAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombreAlumno;

    @Column(nullable = false, length = 50)
    private String matricula;

    @Column(nullable = false)
    private Boolean estadoAlumno = true;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "alumno_id")
    private List<EntityAlumnoAsignatura> listaConEntityAsignatura = new ArrayList<>();

    public EntityAlumno() {
    }

    public EntityAlumno(Long id, String nombreAlumno, String matricula, Boolean estadoAlumno) {
        this.id = id;
        this.nombreAlumno = nombreAlumno;
        this.matricula = matricula;
        this.estadoAlumno = estadoAlumno != null ? estadoAlumno : true;
    }

    public Long getId() {
        return id;
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
        this.estadoAlumno = estadoAlumno != null ? estadoAlumno : true;
    }

    public List<EntityAlumnoAsignatura> getListaConEntityAsignatura() {
        return this.listaConEntityAsignatura;
    }

    public void setListaConEntityAsignatura(List<EntityAlumnoAsignatura> listaConEntityAsignatura) {
        this.listaConEntityAsignatura = listaConEntityAsignatura != null ? listaConEntityAsignatura : new ArrayList<>();
    }

    // Métodos de compatibilidad y conveniencia
    public String getNombre() {
        return this.nombreAlumno;
    }

    public void setNombre(String nombre) {
        this.nombreAlumno = nombre;
    }

    public Boolean getEstado() {
        return this.estadoAlumno;
    }

    public void setEstado(Boolean estado) {
        this.estadoAlumno = estado != null ? estado : true;
    }

    public Long obtenerAsignaturaId() {
        if (listaConEntityAsignatura != null && !listaConEntityAsignatura.isEmpty()) {
            EntityAlumnoAsignatura relacion = listaConEntityAsignatura.get(0);
            return relacion != null ? relacion.getEntityAsignaturaId() : null;
        }
        return null;
    }

    public void asignarAsignatura(Long asignaturaId) {
        if (this.listaConEntityAsignatura == null) {
            this.listaConEntityAsignatura = new ArrayList<>();
        }
        this.listaConEntityAsignatura.clear();
        if (asignaturaId != null) {
            this.listaConEntityAsignatura.add(new EntityAlumnoAsignatura(asignaturaId));
        }
    }
}
