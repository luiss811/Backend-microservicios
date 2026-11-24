package com.maestros.maestros.client;

import com.maestros.maestros.dto.AsignaturaDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "asignaturas", url = "${asignaturas.url:}")
public interface ClientFeignAsignatura {
    @PostMapping("/api/asignaturas/by-ids")
    List<AsignaturaDto> porIds(@RequestBody List<Long> ids);
}
