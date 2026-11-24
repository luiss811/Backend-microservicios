package com.microservicio.alumnos.dto;

import lombok.Data;

@Data
public class EntityAsignaturaDto {
    private Long id;
    private String nombreAsignatura;
    private Boolean estadoAsignatura;
    private String nombre;
    private Boolean estado;
    private Long carreraId;
    private String nombreCarrera;

    public EntityAsignaturaDto() {
    }

    public EntityAsignaturaDto(Long id, String nombreAsignatura) {
        this.id = id;
        this.nombreAsignatura = nombreAsignatura;
        this.nombre = nombreAsignatura;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreAsignatura() {
        if (this.nombreAsignatura != null && !this.nombreAsignatura.isBlank()) {
            return this.nombreAsignatura;
        }
        return this.nombre;
    }

    public void setNombreAsignatura(String nombreAsignatura) {
        this.nombreAsignatura = nombreAsignatura;
        if (this.nombre == null) {
            this.nombre = nombreAsignatura;
        }
    }

    public Boolean getEstadoAsignatura() {
        if (this.estadoAsignatura != null) {
            return this.estadoAsignatura;
        }
        return this.estado;
    }

    public void setEstadoAsignatura(Boolean estadoAsignatura) {
        this.estadoAsignatura = estadoAsignatura;
        if (this.estado == null) {
            this.estado = estadoAsignatura;
        }
    }

    public String getNombre() {
        return getNombreAsignatura();
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        if (this.nombreAsignatura == null) {
            this.nombreAsignatura = nombre;
        }
    }

    public Boolean getEstado() {
        return getEstadoAsignatura();
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
        if (this.estadoAsignatura == null) {
            this.estadoAsignatura = estado;
        }
    }

    public Long getCarreraId() {
        return carreraId;
    }

    public void setCarreraId(Long carreraId) {
        this.carreraId = carreraId;
    }

    public String getNombreCarrera() {
        return nombreCarrera;
    }

    public void setNombreCarrera(String nombreCarrera) {
        this.nombreCarrera = nombreCarrera;
    }
}
