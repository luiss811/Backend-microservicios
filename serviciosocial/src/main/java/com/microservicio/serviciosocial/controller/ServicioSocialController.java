package com.microservicio.serviciosocial.controller;

import com.microservicio.serviciosocial.dto.ServicioSocialDto;
import com.microservicio.serviciosocial.dto.ServicioSocialListDto;
import com.microservicio.serviciosocial.dto.StatusServicioSocialDto;
import com.microservicio.serviciosocial.service.ServicioSocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/servicio-social")
public class ServicioSocialController {

    @Autowired
    private ServicioSocialService service;

    @GetMapping
    public List<ServicioSocialDto> listar() {
        return service.listar();
    }

    @GetMapping("/{id:\\d+}")
    public ServicioSocialDto buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ServicioSocialDto crear(@RequestBody ServicioSocialDto datos) {
        return service.crear(datos);
    }

    @PutMapping("/{id:\\d+}")
    public ServicioSocialDto actualizar(@PathVariable Long id, @RequestBody ServicioSocialDto datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.ok("Registro de Servicio Social eliminado correctamente.");
    }

    @GetMapping("/con-alumno")
    public ResponseEntity<List<ServicioSocialListDto>> listarConAlumnos() {
        return ResponseEntity.ok(service.listarConAlumnos());
    }

    /**
     * Consulta el status y avance de servicio social de un alumno por su ID
     */
    @GetMapping("/alumno/{alumnoId:\\d+}")
    public ResponseEntity<StatusServicioSocialDto> obtenerStatusPorAlumno(@PathVariable Long alumnoId) {
        return ResponseEntity.ok(service.obtenerStatusPorAlumno(alumnoId));
    }

    @PostMapping("/insertar-datos-iniciales")
    public ResponseEntity<List<ServicioSocialDto>> insertarDatosIniciales() {
        return ResponseEntity.ok(service.insertarDatosIniciales());
    }
}
