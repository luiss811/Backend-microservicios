package com.maestros.maestros.client;

import com.maestros.maestros.dto.AsignacionDetalleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "grupos", url = "${grupos.url:}")
public interface ClientFeignGrupo {
    /** maestroId null = todas las asignaciones. */
    @GetMapping("/api/grupos/asignaciones")
    List<AsignacionDetalleDto> asignaciones(@RequestParam(value = "maestroId", required = false) Long maestroId);
}
