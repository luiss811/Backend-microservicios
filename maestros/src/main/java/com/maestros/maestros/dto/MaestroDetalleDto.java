package com.maestros.maestros.dto;

import java.util.List;

/** Detalle: qué materias imparte el maestro y a qué grupos se las da. */
public record MaestroDetalleDto(Long id, String nombre, String correo, Boolean estado, List<Materia> materias) {

    public record Materia(Long asignaturaId, String nombreAsignatura, List<GrupoRef> grupos) {
    }

    public record GrupoRef(Long id, String nombre) {
    }
}
