package com.microservicio.serviciosocial.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "servicio_social")
@Data
public class ServicioSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;

    @Column(nullable = false, length = 150)
    private String institucion;

    @Column(length = 150)
    private String programa;

    @Column(nullable = false)
    private Integer horasAcumuladas = 0;

    @Column(nullable = false)
    private Integer horasTotales = 480;

    @Column(nullable = false, length = 50)
    private String estado = "EN_PROCESO";

    @Column(length = 50)
    private String fechaInicio;
}
