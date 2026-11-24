package com.grupos.grupos.dto;

import java.util.List;

/** Entrada y salida básica del CRUD. Cada asignación = materia + maestro que la imparte en el grupo. */
public record GrupoDto(Long id, String nombre, Boolean estado, List<Asignacion> asignaciones) {

    public record Asignacion(Long asignaturaId, Long maestroId) {
    }
}
