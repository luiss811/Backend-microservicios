package com.microservicio.asignaturas.dto;

import lombok.Data;

@Data
public class AsignaturaDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean estado;
    private Long carreraId;
    private String nombreCarrera;

    public AsignaturaDto() {
    }

    public AsignaturaDto(Long id, String nombre, String descripcion, Boolean estado, Long carreraId, String nombreCarrera) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.estado = estado;
        this.carreraId = carreraId;
        this.nombreCarrera = nombreCarrera;
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

    public String getNombreCarrera() {
        return nombreCarrera;
    }

    public void setNombreCarrera(String nombreCarrera) {
        this.nombreCarrera = nombreCarrera;
    }

    // Métodos de compatibilidad para frontend y otros microservicios
    public String getNombreAsignatura() {
        return this.nombre;
    }

    public void setNombreAsignatura(String nombreAsignatura) {
        if (this.nombre == null) {
            this.nombre = nombreAsignatura;
        }
    }

    public Boolean getEstadoAsignatura() {
        return this.estado;
    }

    public void setEstadoAsignatura(Boolean estadoAsignatura) {
        if (this.estado == null) {
            this.estado = estadoAsignatura;
        }
    }
}
