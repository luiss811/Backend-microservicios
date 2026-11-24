package com.grupos.grupos.client;

import com.grupos.grupos.dto.MaestroDto;
import com.grupos.grupos.dto.OpcionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "maestros", url = "${maestros.url:}")
public interface ClientFeignMaestro {
    @GetMapping("/api/maestros/opciones")
    List<OpcionDto> opciones(@RequestParam("asignaturaId") List<Long> asignaturaIds);

    @PostMapping("/api/maestros/by-ids")
    List<MaestroDto> porIds(@RequestBody List<Long> ids);
}
