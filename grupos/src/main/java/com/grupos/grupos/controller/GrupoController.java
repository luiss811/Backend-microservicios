package com.grupos.grupos.controller;

import com.grupos.grupos.dto.AsignacionDetalleDto;
import com.grupos.grupos.dto.GrupoDetalleDto;
import com.grupos.grupos.dto.GrupoDto;
import com.grupos.grupos.service.GrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService service;

    @GetMapping
    public List<GrupoDto> listar() {
        return service.listar();
    }

    @GetMapping("/{id:\\d+}")
    public GrupoDto buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<GrupoDto> crear(@RequestBody GrupoDto datos) {
        return ResponseEntity.status(201).body(service.crear(datos));
    }

    @PutMapping("/{id:\\d+}")
    public GrupoDto actualizar(@PathVariable Long id, @RequestBody GrupoDto datos) {
        return service.actualizar(id, datos);
    }

    @PatchMapping("/{id:\\d+}/desactivar")
    public GrupoDto desactivar(@PathVariable Long id) {
        return service.cambiarEstado(id, false);
    }

    @PatchMapping("/{id:\\d+}/activar")
    public GrupoDto activar(@PathVariable Long id) {
        return service.cambiarEstado(id, true);
    }

    /** Qué materias tiene cada grupo y con qué maestro. */
    @GetMapping("/detalle")
    public List<GrupoDetalleDto> detalleTodos() {
        return service.detalleTodos();
    }

    @GetMapping("/{id:\\d+}/detalle")
    public GrupoDetalleDto detalle(@PathVariable Long id) {
        return service.detalle(id);
    }

    /** Lo consume Maestros: materias que se imparten en grupos (?maestroId= para filtrar). */
    @GetMapping("/asignaciones")
    public List<AsignacionDetalleDto> asignaciones(@RequestParam(required = false) Long maestroId) {
        return service.asignaciones(maestroId);
    }
}
