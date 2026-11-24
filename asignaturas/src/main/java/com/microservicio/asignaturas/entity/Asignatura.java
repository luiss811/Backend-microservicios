package com.microservicio.asignaturas.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "asignaturas")
@Data
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Boolean estado = true;

    // ID de Carrera en el microservicio externo.
    // No se usa @ManyToOne porque Carrera pertenece a otro microservicio.
    @Column(name = "carrera_id", nullable = false)
    private Long carreraId;

    public Asignatura() {
    }

    public Asignatura(Long id, String nombre, String descripcion, Boolean estado, Long carreraId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.carreraId = carreraId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Long getCarreraId() {
        return carreraId;
    }

    public void setCarreraId(Long carreraId) {
        this.carreraId = carreraId;
    }
}
