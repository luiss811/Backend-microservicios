package com.maestros.maestros.service;

import com.maestros.maestros.client.ClientFeignAsignatura;
import com.maestros.maestros.client.ClientFeignGrupo;
import com.maestros.maestros.dto.*;
import com.maestros.maestros.entity.Maestro;
import com.maestros.maestros.entity.MaestroAsignatura;
import com.maestros.maestros.repository.MaestroAsignaturaRepository;
import com.maestros.maestros.repository.MaestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaestroService {

    private final MaestroRepository repository;
    private final MaestroAsignaturaRepository relaciones;
    private final ClientFeignAsignatura clientAsignaturas;
    private final ClientFeignGrupo clientGrupos;

    // ---------- CRUD ----------

    @Transactional(readOnly = true)
    public List<MaestroDto> listar() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public MaestroDto buscar(Long id) {
        return toDto(obtener(id));
    }

    @Transactional
    public MaestroDto crear(MaestroDto datos) {
        Maestro maestro = new Maestro();
        maestro.setNombre(nombreValido(datos.nombre()));
        maestro.setCorreo(datos.correo());
        List<Long> ids = idsUnicos(datos.asignaturaIds());
        validarExistenciaAsignaturas(ids);
        ids.forEach(a -> agregar(maestro, a));
        return toDto(repository.save(maestro));
    }

    @Transactional
    public MaestroDto actualizar(Long id, MaestroDto datos) {
        Maestro maestro = obtener(id);
        if (datos.nombre() != null) {
            maestro.setNombre(nombreValido(datos.nombre()));
        }
        if (datos.correo() != null) {
            maestro.setCorreo(datos.correo());
        }
        // null = sin cambios; lista vacía = quita todas las materias.
        if (datos.asignaturaIds() != null) {
            sincronizar(maestro, idsUnicos(datos.asignaturaIds()));
        }
        return toDto(repository.save(maestro));
    }

    @Transactional
    public MaestroDto cambiarEstado(Long id, boolean estado) {
        Maestro maestro = obtener(id);
        maestro.setEstado(estado);
        return toDto(repository.save(maestro));
    }

    // ---------- Consumidos por Grupos ----------

    /** Pares materia + maestro activo que la imparte (dropdown del formulario de grupos). */
    @Transactional(readOnly = true)
    public List<OpcionDto> opciones(List<Long> asignaturaIds) {
        List<MaestroAsignatura> rels = asignaturaIds == null || asignaturaIds.isEmpty()
                ? relaciones.findAll()
                : relaciones.findByAsignaturaIdIn(asignaturaIds);
        rels = rels.stream().filter(r -> Boolean.TRUE.equals(r.getMaestro().getEstado())).toList();
        Map<Long, String> nombres = nombresAsignaturas(
                rels.stream().map(MaestroAsignatura::getAsignaturaId).collect(Collectors.toSet()));
        return rels.stream()
                .map(r -> new OpcionDto(r.getAsignaturaId(), nombreAsignatura(nombres, r.getAsignaturaId()),
                        r.getMaestro().getId(), r.getMaestro().getNombre()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaestroDto> obtenerPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(ids).stream().map(this::toDto).toList();
    }

    // ---------- Detalle: materias que imparte y a qué grupos ----------

    @Transactional(readOnly = true)
    public MaestroDetalleDto detalle(Long id) {
        Maestro maestro = obtener(id);
        List<AsignacionDetalleDto> usos = asignacionesEnGrupos(id);
        return toDetalle(maestro, usos, nombresAsignaturas(idsDe(maestro)));
    }

    @Transactional(readOnly = true)
    public List<MaestroDetalleDto> detalleTodos() {
        List<Maestro> maestros = repository.findAll();
        List<AsignacionDetalleDto> usos = asignacionesEnGrupos(null);
        Set<Long> ids = maestros.stream().flatMap(m -> idsDe(m).stream()).collect(Collectors.toSet());
        Map<Long, String> nombres = nombresAsignaturas(ids);
        return maestros.stream()
                .map(m -> toDetalle(m,
                        usos.stream().filter(u -> m.getId().equals(u.maestroId())).toList(), nombres))
                .toList();
    }

    // ---------- Internos ----------

    private Maestro obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maestro no encontrado"));
    }

    private MaestroDto toDto(Maestro m) {
        return new MaestroDto(m.getId(), m.getNombre(), m.getCorreo(), m.getEstado(), idsDe(m));
    }

    private MaestroDetalleDto toDetalle(Maestro m, List<AsignacionDetalleDto> usos, Map<Long, String> nombres) {
        List<MaestroDetalleDto.Materia> materias = m.getAsignaturas().stream()
                .map(r -> new MaestroDetalleDto.Materia(
                        r.getAsignaturaId(),
                        nombreAsignatura(nombres, r.getAsignaturaId()),
                        usos.stream()
                                .filter(u -> r.getAsignaturaId().equals(u.asignaturaId()))
                                .map(u -> new MaestroDetalleDto.GrupoRef(u.grupoId(), u.nombreGrupo()))
                                .toList()))
                .toList();
        return new MaestroDetalleDto(m.getId(), m.getNombre(), m.getCorreo(), m.getEstado(), materias);
    }

    private List<Long> idsDe(Maestro m) {
        return m.getAsignaturas().stream().map(MaestroAsignatura::getAsignaturaId).toList();
    }

    private void agregar(Maestro maestro, Long asignaturaId) {
        MaestroAsignatura rel = new MaestroAsignatura();
        rel.setMaestro(maestro);
        rel.setAsignaturaId(asignaturaId);
        maestro.getAsignaturas().add(rel);
    }

    /** Aplica solo las diferencias para no chocar con la restricción única (maestro, asignatura). */
    private void sincronizar(Maestro maestro, List<Long> nuevas) {
        Set<Long> actuales = new HashSet<>(idsDe(maestro));
        Set<Long> quitadas = new HashSet<>(actuales);
        quitadas.removeAll(nuevas);
        List<Long> agregadas = nuevas.stream().filter(a -> !actuales.contains(a)).toList();

        if (!quitadas.isEmpty()) {
            validarSinUsoEnGrupos(maestro.getId(), quitadas);
        }
        validarExistenciaAsignaturas(agregadas);

        maestro.getAsignaturas().removeIf(r -> quitadas.contains(r.getAsignaturaId()));
        agregadas.forEach(a -> agregar(maestro, a));
    }

    private void validarSinUsoEnGrupos(Long maestroId, Set<Long> quitadas) {
        List<String> conflictos = asignacionesEnGrupos(maestroId).stream()
                .filter(u -> quitadas.contains(u.asignaturaId()))
                .map(u -> u.nombreAsignatura() + " (grupo " + u.nombreGrupo() + ")")
                .toList();
        if (!conflictos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se pueden quitar materias asignadas en grupos: " + String.join(", ", conflictos));
        }
    }

    private void validarExistenciaAsignaturas(Collection<Long> ids) {
        Map<Long, String> existentes = nombresAsignaturas(ids);
        List<Long> faltantes = ids.stream().filter(a -> !existentes.containsKey(a)).toList();
        if (!faltantes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Asignaturas inexistentes en Microservicio-Asignaturas: " + faltantes);
        }
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

    private List<AsignacionDetalleDto> asignacionesEnGrupos(Long maestroId) {
        try {
            return clientGrupos.asignaciones(maestroId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar al Microservicio-Grupos: " + e.getMessage());
        }
    }

    private String nombreAsignatura(Map<Long, String> nombres, Long id) {
        return nombres.getOrDefault(id, "Asignatura #" + id);
    }

    private String nombreValido(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (nombre.trim().length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede superar 120 caracteres");
        }
        return nombre.trim();
    }

    private List<Long> idsUnicos(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        if (ids.contains(null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "asignaturaIds contiene valores nulos");
        }
        return ids.stream().distinct().toList();
    }
}
