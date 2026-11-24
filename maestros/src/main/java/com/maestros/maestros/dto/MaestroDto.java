package com.maestros.maestros.dto;

import java.util.List;

/** Entrada y salida básica del CRUD. asignaturaIds = materias que imparte. */
public record MaestroDto(Long id, String nombre, String correo, Boolean estado, List<Long> asignaturaIds) {
}
