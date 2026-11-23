package com.microservicio.serviciosocial.dto;

import lombok.Data;

@Data
public class ServicioSocialListDto {
    private Long id;
    private Long alumnoId;
    private String nombreAlumno;
    private String matricula;
    private String institucion;
    private String programa;
    private Integer horasAcumuladas;
    private Integer horasTotales;
    private String estado;
    private String fechaInicio;
}
