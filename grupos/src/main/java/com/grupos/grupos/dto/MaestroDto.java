package com.grupos.grupos.dto;

/** Respuesta de Maestros (POST /api/maestros/by-ids); campos extra se ignoran. */
public record MaestroDto(Long id, String nombre, Boolean estado) {
}
