package com.maestros.maestros.dto;

/** Respuesta de Grupos (GET /api/grupos/asignaciones). */
public record AsignacionDetalleDto(Long grupoId, String nombreGrupo, Long asignaturaId, String nombreAsignatura,
        Long maestroId) {
}
