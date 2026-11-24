package com.maestros.maestros.dto;

/** Opción del dropdown del formulario de grupos: materia + maestro que la imparte. */
public record OpcionDto(Long asignaturaId, String nombreAsignatura, Long maestroId, String nombreMaestro) {
}
