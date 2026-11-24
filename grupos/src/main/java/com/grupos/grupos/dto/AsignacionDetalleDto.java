package com.grupos.grupos.dto;

/** Lo consume Maestros para armar su detalle (materia que da y en qué grupo). */
public record AsignacionDetalleDto(Long grupoId, String nombreGrupo, Long asignaturaId, String nombreAsignatura,
        Long maestroId) {
}
