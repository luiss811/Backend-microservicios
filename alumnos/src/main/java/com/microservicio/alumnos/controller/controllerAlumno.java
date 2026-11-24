package com.microservicio.alumnos.controller;

import com.microservicio.alumnos.dto.EntityAlumnoDtoList;
import com.microservicio.alumnos.dto.dto;
import com.microservicio.alumnos.service.servicio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/alumno")
public class controllerAlumno {

    private final servicio servicio;

    public controllerAlumno(servicio servicio) {
        this.servicio = servicio;
    }

    // Consulta general de alumnos
    @GetMapping
    public List<dto> listar() {
        return servicio.listar();
    }

    // Consulta por ID
    @GetMapping("/{id:\\d+}")
    public dto buscar(@PathVariable Long id) {
        return servicio.buscar(id);
    }

    // Alta de alumno
    @PostMapping
    public ResponseEntity<dto> crear(@RequestBody dto datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.crear(datos));
    }

    // Edición de alumno
    @PutMapping("/{id}")
    public dto actualizar(@PathVariable Long id, @RequestBody dto datos) {
        return servicio.actualizar(id, datos);
    }

    // Activar alumno
    @PatchMapping("/{id}/activar")
    public dto activar(@PathVariable Long id) {
        return servicio.cambiarEstado(id, true);
    }

    // Desactivar alumno
    @PatchMapping("/{id}/desactivar")
    public dto desactivar(@PathVariable Long id) {
        return servicio.cambiarEstado(id, false);
    }

    // Baja permanente
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.ok("Alumno eliminado correctamente!");
    }

    // Consulta combinada mediante OpenFeign hacia el microservicio de Asignaturas
    @GetMapping("/con-asignatura")
    public ResponseEntity<List<EntityAlumnoDtoList>> getAll() {
        List<EntityAlumnoDtoList> list = servicio.findAll();
        return ResponseEntity.ok(list);
    }

    // Carga inicial de datos de prueba
    @PostMapping("/insertar-alumnos-iniciales")
    public ResponseEntity<List<dto>> insertarDatosIniciales() {
        List<dto> registros = new ArrayList<>();

        String[][] alumnosData = {
            {"Erik", "2023171006", "false"},
            {"Valeria", "2023171007", "true"},
            {"Karen", "2023171008", "true"},
            {"Emilio", "2023171009", "true"},
            {"Saul", "2023171010", "true"},
            {"Luis", "2023171011", "true"}
        };

        for (String[] alum : alumnosData) {
            dto nuevo = new dto();
            nuevo.setNombreAlumno(alum[0]);
            nuevo.setMatricula(alum[1]);
            nuevo.setEstadoAlumno(Boolean.parseBoolean(alum[2]));
            nuevo.setEntityAsignaturaId(null);
            registros.add(servicio.crear(nuevo));
        }

        return ResponseEntity.ok(registros);
    }
}
