package com.maestros.maestros.dto;

/** Respuesta de Asignaturas (POST /api/asignaturas/by-ids). */
public record AsignaturaDto(Long id, String nombreAsignatura) {
}
