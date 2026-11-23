package com.microservicio.serviciosocial.client;

import com.microservicio.serviciosocial.dto.AlumnoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "alumnos", url = "${alumnos.url:}")
public interface ClientFeignAlumno {

    @GetMapping("/api/alumno/{id}")
    AlumnoDto buscarPorId(@PathVariable("id") Long id);

    @GetMapping("/api/alumno")
    List<AlumnoDto> listarTodos();
}
