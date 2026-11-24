package com.microservicio.asignaturas.dto;

import lombok.Data;

@Data
public class CarreraDto {
    private Long id;
    private String nombre;
    private Boolean estado;
    private String descripcion;

    public CarreraDto() {
    }

    public CarreraDto(Long id, String nombre, Boolean estado, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
        this.descripcion = descripcion;
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

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
