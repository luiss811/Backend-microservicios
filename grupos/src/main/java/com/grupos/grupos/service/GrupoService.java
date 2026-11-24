package com.grupos.grupos.service;

import com.grupos.grupos.client.ClientFeignAsignatura;
import com.grupos.grupos.client.ClientFeignMaestro;
import com.grupos.grupos.dto.*;
import com.grupos.grupos.entity.Grupo;
import com.grupos.grupos.entity.GrupoAsignaturaMaestro;
import com.grupos.grupos.repository.GrupoAsignaturaMaestroRepository;
import com.grupos.grupos.repository.GrupoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository repository;
    private final GrupoAsignaturaMaestroRepository relaciones;
    private final ClientFeignAsignatura clientAsignaturas;
    private final ClientFeignMaestro clientMaestros;

    // ---------- CRUD ----------

    @Transactional(readOnly = true)
    public List<GrupoDto> listar() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public GrupoDto buscar(Long id) {
        return toDto(obtener(id));
    }

    @Transactional
    public GrupoDto crear(GrupoDto datos) {
        Grupo grupo = new Grupo();
        grupo.setNombre(nombreValido(datos.nombre()));
        validarAsignaciones(datos.asignaciones()).forEach(a -> agregar(grupo, a));
        return toDto(repository.save(grupo));
    }

    @Transactional
    public GrupoDto actualizar(Long id, GrupoDto datos) {
        Grupo grupo = obtener(id);
        if (datos.nombre() != null) {
            grupo.setNombre(nombreValido(datos.nombre()));
        }
        // null = sin cambios; lista vacía = quita todas las materias.
        if (datos.asignaciones() != null) {
            sincronizar(grupo, validarAsignaciones(datos.asignaciones()));
        }
        return toDto(repository.save(grupo));
    }

    @Transactional
    public GrupoDto cambiarEstado(Long id, boolean estado) {
        Grupo grupo = obtener(id);
        grupo.setEstado(estado);
        return toDto(repository.save(grupo));
    }

    // ---------- Detalle: materias del grupo y con qué maestro ----------

    @Transactional(readOnly = true)
    public GrupoDetalleDto detalle(Long id) {
        return detalles(List.of(obtener(id))).get(0);
    }

    @Transactional(readOnly = true)
    public List<GrupoDetalleDto> detalleTodos() {
        return detalles(repository.findAll());
    }

    // ---------- Consumido por Maestros ----------

    /** Materias que se imparten en grupos; maestroId null = todas. */
    @Transactional(readOnly = true)
    public List<AsignacionDetalleDto> asignaciones(Long maestroId) {
        List<GrupoAsignaturaMaestro> rels = maestroId == null ? relaciones.findAll()
                : relaciones.findByMaestroId(maestroId);
        Map<Long, String> asignaturas = nombresAsignaturas(
                rels.stream().map(GrupoAsignaturaMaestro::getAsignaturaId).collect(Collectors.toSet()));
        return rels.stream()
                .map(r -> new AsignacionDetalleDto(r.getGrupo().getId(), r.getGrupo().getNombre(),
                        r.getAsignaturaId(), nombre(asignaturas, "Asignatura", r.getAsignaturaId()),
                        r.getMaestroId()))
                .toList();
    }

    // ---------- Internos ----------

    private Grupo obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo no encontrado"));
    }

    private GrupoDto toDto(Grupo g) {
        return new GrupoDto(g.getId(), g.getNombre(), g.getEstado(),
                g.getAsignaciones().stream()
                        .map(r -> new GrupoDto.Asignacion(r.getAsignaturaId(), r.getMaestroId()))
                        .toList());
    }

    /** Resuelve los nombres de todos los grupos con una sola llamada por microservicio. */
    private List<GrupoDetalleDto> detalles(List<Grupo> grupos) {
        List<GrupoAsignaturaMaestro> todas = grupos.stream().flatMap(g -> g.getAsignaciones().stream()).toList();
        Map<Long, String> asignaturas = nombresAsignaturas(
                todas.stream().map(GrupoAsignaturaMaestro::getAsignaturaId).collect(Collectors.toSet()));
        Map<Long, String> maestros = nombresMaestros(
                todas.stream().map(GrupoAsignaturaMaestro::getMaestroId).collect(Collectors.toSet()));
        return grupos.stream()
                .map(g -> new GrupoDetalleDto(g.getId(), g.getNombre(), g.getEstado(),
                        g.getAsignaciones().stream()
                                .map(r -> new GrupoDetalleDto.Materia(
                                        r.getAsignaturaId(), nombre(asignaturas, "Asignatura", r.getAsignaturaId()),
                                        r.getMaestroId(), nombre(maestros, "Maestro", r.getMaestroId())))
                                .toList()))
                .toList();
    }

    private void agregar(Grupo grupo, GrupoDto.Asignacion a) {
        GrupoAsignaturaMaestro rel = new GrupoAsignaturaMaestro();
        rel.setGrupo(grupo);
        rel.setAsignaturaId(a.asignaturaId());
        rel.setMaestroId(a.maestroId());
        grupo.getAsignaciones().add(rel);
    }

    /** Aplica solo las diferencias para no chocar con la restricción única (grupo, asignatura). */
    private void sincronizar(Grupo grupo, List<GrupoDto.Asignacion> nuevas) {
        Set<Long> ids = nuevas.stream().map(GrupoDto.Asignacion::asignaturaId).collect(Collectors.toSet());
        grupo.getAsignaciones().removeIf(r -> !ids.contains(r.getAsignaturaId()));
        for (GrupoDto.Asignacion a : nuevas) {
            grupo.getAsignaciones().stream()
                    .filter(r -> r.getAsignaturaId().equals(a.asignaturaId()))
                    .findFirst()
                    .ifPresentOrElse(r -> r.setMaestroId(a.maestroId()), () -> agregar(grupo, a));
        }
    }

    /** Cada materia debe existir y el maestro indicado debe estar activo e impartirla. */
    private List<GrupoDto.Asignacion> validarAsignaciones(List<GrupoDto.Asignacion> asignaciones) {
        if (asignaciones == null || asignaciones.isEmpty()) {
            return List.of();
        }
        if (asignaciones.stream().anyMatch(a -> a == null || a.asignaturaId() == null || a.maestroId() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cada asignación requiere asignaturaId y maestroId");
        }
        List<Long> ids = asignaciones.stream().map(GrupoDto.Asignacion::asignaturaId).toList();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Una materia no puede repetirse en el mismo grupo");
        }

        Map<Long, String> existentes = nombresAsignaturas(ids);
        List<Long> faltantes = ids.stream().filter(a -> !existentes.containsKey(a)).toList();
        if (!faltantes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Asignaturas inexistentes en Microservicio-Asignaturas: " + faltantes);
        }

        Set<String> validos = opcionesMaestros(ids).stream()
                .map(o -> o.asignaturaId() + ":" + o.maestroId())
                .collect(Collectors.toSet());
        for (GrupoDto.Asignacion a : asignaciones) {
            if (!validos.contains(a.asignaturaId() + ":" + a.maestroId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El maestro #" + a.maestroId()
                        + " no imparte la asignatura #" + a.asignaturaId() + " o está inactivo");
            }
        }
        return asignaciones;
    }

    private Map<Long, String> nombresAsignaturas(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            return clientAsignaturas.porIds(List.copyOf(ids)).stream()
                    .collect(Collectors.toMap(AsignaturaDto::id, AsignaturaDto::nombreAsignatura));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar al Microservicio-Asignaturas: " + e.getMessage());
        }
    }

    private Map<Long, String> nombresMaestros(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            return clientMaestros.porIds(List.copyOf(ids)).stream()
                    .collect(Collectors.toMap(MaestroDto::id, MaestroDto::nombre));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar al Microservicio-Maestros: " + e.getMessage());
        }
    }

    private List<OpcionDto> opcionesMaestros(List<Long> asignaturaIds) {
        try {
            return clientMaestros.opciones(asignaturaIds);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar al Microservicio-Maestros: " + e.getMessage());
        }
    }

    private String nombre(Map<Long, String> nombres, String tipo, Long id) {
        return nombres.getOrDefault(id, tipo + " #" + id);
    }

    private String nombreValido(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (nombre.trim().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede superar 100 caracteres");
        }
        return nombre.trim();
    }
}
