package com.maestros.maestros.controller;

import com.maestros.maestros.dto.MaestroDetalleDto;
import com.maestros.maestros.dto.MaestroDto;
import com.maestros.maestros.dto.OpcionDto;
import com.maestros.maestros.service.MaestroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maestros")
@RequiredArgsConstructor
public class MaestroController {

    private final MaestroService service;

    @GetMapping
    public List<MaestroDto> listar() {
        return service.listar();
    }

    @GetMapping("/{id:\\d+}")
    public MaestroDto buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<MaestroDto> crear(@RequestBody MaestroDto datos) {
        return ResponseEntity.status(201).body(service.crear(datos));
    }

    @PutMapping("/{id:\\d+}")
    public MaestroDto actualizar(@PathVariable Long id, @RequestBody MaestroDto datos) {
        return service.actualizar(id, datos);
    }

    @PatchMapping("/{id:\\d+}/desactivar")
    public MaestroDto desactivar(@PathVariable Long id) {
        return service.cambiarEstado(id, false);
    }

    @PatchMapping("/{id:\\d+}/activar")
    public MaestroDto activar(@PathVariable Long id) {
        return service.cambiarEstado(id, true);
    }

    /** Dropdown de grupos: ?asignaturaId=1&asignaturaId=2 (sin parámetro = todas). */
    @GetMapping("/opciones")
    public List<OpcionDto> opciones(@RequestParam(required = false) List<Long> asignaturaId) {
        return service.opciones(asignaturaId);
    }

    /** Lo consume Grupos para resolver nombres de maestros. */
    @PostMapping("/by-ids")
    public List<MaestroDto> obtenerPorIds(@RequestBody List<Long> ids) {
        return service.obtenerPorIds(ids);
    }

    /** Qué materias da cada maestro y a qué grupos. */
    @GetMapping("/detalle")
    public List<MaestroDetalleDto> detalleTodos() {
        return service.detalleTodos();
    }

    @GetMapping("/{id:\\d+}/detalle")
    public MaestroDetalleDto detalle(@PathVariable Long id) {
        return service.detalle(id);
    }
}
