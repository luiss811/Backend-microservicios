package com.microservicio.alumnos.client;

import com.microservicio.alumnos.dto.EntityAsignaturaDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "asignaturas", url = "${asignaturas.url:}")
public interface ClientFeignAsignatura {

    @PostMapping("/api/asignaturas/by-ids")
    List<EntityAsignaturaDto> obtenerDTOsDelAsignatura(@RequestBody List<Long> ids);

    @GetMapping("/api/asignaturas/{id}")
    EntityAsignaturaDto obtenerAsignaturaPorId(@PathVariable("id") Long id);

    @GetMapping("/api/asignaturas")
    List<EntityAsignaturaDto> listarAsignaturas();
}
