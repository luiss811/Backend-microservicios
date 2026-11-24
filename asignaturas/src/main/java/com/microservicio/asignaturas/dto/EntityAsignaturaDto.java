package com.microservicio.asignaturas.dto;

import lombok.Data;

@Data
public class EntityAsignaturaDto {
    private Long id;
    private String nombreAsignatura;

    public EntityAsignaturaDto() {
    }

    public EntityAsignaturaDto(Long id, String nombreAsignatura) {
        this.id = id;
        this.nombreAsignatura = nombreAsignatura;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreAsignatura() {
        return nombreAsignatura;
    }

    public void setNombreAsignatura(String nombreAsignatura) {
        this.nombreAsignatura = nombreAsignatura;
    }

    // Compatibilidad en caso de lectura por campo 'nombre'
    public String getNombre() {
        return this.nombreAsignatura;
    }

    public void setNombre(String nombre) {
        if (this.nombreAsignatura == null) {
            this.nombreAsignatura = nombre;
        }
    }
}
