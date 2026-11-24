package com.grupos.grupos.dto;

/** Respuesta de Maestros (GET /api/maestros/opciones). */
public record OpcionDto(Long asignaturaId, String nombreAsignatura, Long maestroId, String nombreMaestro) {
}
