package com.grupos.grupos.dto;

import java.util.List;

/** Detalle: qué materias tiene el grupo y con qué maestro. */
public record GrupoDetalleDto(Long id, String nombre, Boolean estado, List<Materia> materias) {

    public record Materia(Long asignaturaId, String nombreAsignatura, Long maestroId, String nombreMaestro) {
    }
}
