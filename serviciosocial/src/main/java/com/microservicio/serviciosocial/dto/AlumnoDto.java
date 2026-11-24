package com.microservicio.serviciosocial.dto;

import lombok.Data;

@Data
public class AlumnoDto {
    private Long id;
    private String nombreAlumno;
    private String matricula;
    private Boolean estadoAlumno;
    private Long entityAsignaturaId;
}
