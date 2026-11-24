package com.microservicio.serviciosocial.dto;

import lombok.Data;

@Data
public class ServicioSocialDto {
    private Long id;
    private Long alumnoId;
    private String institucion;
    private String programa;
    private Integer horasAcumuladas;
    private Integer horasTotales;
    private String estado;
    private String fechaInicio;
}
