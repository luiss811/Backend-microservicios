package com.microservicio.serviciosocial.dto;

import lombok.Data;

@Data
public class StatusServicioSocialDto {
    private Long alumnoId;
    private String nombreAlumno;
    private String matricula;
    private Boolean tieneServicio;
    private Long servicioSocialId;
    private String institucion;
    private String programa;
    private Integer horasAcumuladas;
    private Integer horasTotales;
    private Double porcentajeAvance;
    private String estado; // EN_PROCESO, LIBERADO, SIN_SERVICIO
    private String fechaInicio;
    private String mensaje;
}
