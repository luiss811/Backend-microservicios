package com.microservicio.alumnos.service;

import com.microservicio.alumnos.client.ClientFeignAsignatura;
import com.microservicio.alumnos.dto.EntityAlumnoDtoList;
import com.microservicio.alumnos.dto.EntityAsignaturaDto;
import com.microservicio.alumnos.dto.dto;
import com.microservicio.alumnos.entity.EntityAlumno;
import com.microservicio.alumnos.entity.EntityAlumnoAsignatura;
import com.microservicio.alumnos.repository.repositoryAlumno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class servicio {

    @Autowired
    private repositoryAlumno repositoryAlumno;

    @Autowired
    private ClientFeignAsignatura clientFeignAsignatura;

    public dto convertirDTO(EntityAlumno entityAlumno) {
        dto datos = new dto();
        datos.setId(entityAlumno.getId());
        datos.setNombreAlumno(entityAlumno.getNombreAlumno());
        datos.setMatricula(entityAlumno.getMatricula());
        datos.setEstadoAlumno(entityAlumno.getEstadoAlumno());

        List<EntityAlumnoAsignatura> relaciones = entityAlumno.getListaConEntityAsignatura();
        if (relaciones != null && !relaciones.isEmpty() && relaciones.get(0).getEntityAsignaturaId() != null) {
            datos.setEntityAsignaturaId(relaciones.get(0).getEntityAsignaturaId());
        }
        return datos;
    }

    /** Verifica en Asignaturas (Feign) que exista la asignatura; null = sin relación. */
    private void validarEntityAsignatura(Long entityAsignaturaId) {
        if (entityAsignaturaId == null) {
            return;
        }
        try {
            List<EntityAsignaturaDto> encontrados = clientFeignAsignatura
                    .obtenerDTOsDelAsignatura(List.of(entityAsignaturaId));
            boolean existe = encontrados != null && encontrados.stream()
                    .anyMatch(a -> a != null && entityAsignaturaId.equals(a.getId()));
            if (!existe) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La Asignatura #" + entityAsignaturaId + " no existe en Microservicio-Asignaturas");
            }
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar al Microservicio-Asignaturas para verificar la asignatura: " + e.getMessage());
        }
    }

    /** Deja al alumno relacionado solo con entityAsignaturaId (o sin relación si es null). */
    private void asignarEntityAsignatura(EntityAlumno entityAlumno, Long entityAsignaturaId) {
        if (entityAlumno.getListaConEntityAsignatura() == null) {
            entityAlumno.setListaConEntityAsignatura(new ArrayList<>());
        }
        entityAlumno.getListaConEntityAsignatura().clear();
        if (entityAsignaturaId != null) {
            EntityAlumnoAsignatura relacion = new EntityAlumnoAsignatura();
            relacion.setEntityAsignaturaId(entityAsignaturaId);
            entityAlumno.getListaConEntityAsignatura().add(relacion);
        }
    }

    private EntityAlumno obtener(Long id) {
        return repositoryAlumno.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado!"));
    }

    @Transactional(readOnly = true)
    public List<dto> listar() {
        return repositoryAlumno.findAll()
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public dto buscar(Long id) {
        return convertirDTO(obtener(id));
    }

    @Transactional
    public dto crear(dto datos) {
        validarEntityAsignatura(datos.getEntityAsignaturaId());
        EntityAlumno nuevo = new EntityAlumno();
        nuevo.setNombreAlumno(datos.getNombreAlumno() != null ? datos.getNombreAlumno() : "Sin nombre");
        nuevo.setMatricula(datos.getMatricula() != null ? datos.getMatricula() : "Sin matricula");
        nuevo.setEstadoAlumno(datos.getEstadoAlumno() != null ? datos.getEstadoAlumno() : true);
        asignarEntityAsignatura(nuevo, datos.getEntityAsignaturaId());
        return convertirDTO(repositoryAlumno.save(nuevo));
    }

    @Transactional
    public dto actualizar(Long id, dto datos) {
        EntityAlumno registro = obtener(id);
        validarEntityAsignatura(datos.getEntityAsignaturaId());

        if (datos.getNombreAlumno() != null) {
            registro.setNombreAlumno(datos.getNombreAlumno());
        }
        if (datos.getMatricula() != null) {
            registro.setMatricula(datos.getMatricula());
        }
        if (datos.getEstadoAlumno() != null) {
            registro.setEstadoAlumno(datos.getEstadoAlumno());
        }
        asignarEntityAsignatura(registro, datos.getEntityAsignaturaId());

        return convertirDTO(repositoryAlumno.save(registro));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repositoryAlumno.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alumno no encontrado!");
        }
        repositoryAlumno.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EntityAlumnoDtoList> findAll() {
        List<EntityAlumno> datosAlumno = repositoryAlumno.findAll();
        List<Long> idsRelacionados = datosAlumno.stream()
                .filter(alumno -> alumno.getListaConEntityAsignatura() != null)
                .flatMap(alumno -> alumno.getListaConEntityAsignatura().stream())
                .map(EntityAlumnoAsignatura::getEntityAsignaturaId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> asignaturaMap = new HashMap<>();
        if (!idsRelacionados.isEmpty()) {
            try {
                List<EntityAsignaturaDto> listaAsignaturas = clientFeignAsignatura
                        .obtenerDTOsDelAsignatura(idsRelacionados);
                if (listaAsignaturas != null) {
                    for (EntityAsignaturaDto asig : listaAsignaturas) {
                        if (asig != null && asig.getId() != null) {
                            asignaturaMap.put(asig.getId(), asig.getNombreAsignatura());
                        }
                    }
                }
            } catch (Exception e) {
                // Si Microservicio-Asignaturas no responde, se muestra "Asignatura #id".
            }
        }

        return datosAlumno.stream()
                .map(e -> {
                    EntityAlumnoDtoList dtoResult = new EntityAlumnoDtoList();
                    dtoResult.setId(e.getId());
                    dtoResult.setNombreAlumno(e.getNombreAlumno());
                    dtoResult.setMatricula(e.getMatricula());
                    dtoResult.setEstadoAlumno(e.getEstadoAlumno());

                    List<EntityAlumnoAsignatura> relaciones = e.getListaConEntityAsignatura();
                    if (relaciones != null && !relaciones.isEmpty()
                            && relaciones.get(0).getEntityAsignaturaId() != null) {
                        Long asignaturaId = relaciones.get(0).getEntityAsignaturaId();
                        dtoResult.setEntityAsignaturaId(asignaturaId);
                        String nombre = asignaturaMap.get(asignaturaId);
                        dtoResult.setNombreAsignatura(nombre != null ? nombre : "Asignatura #" + asignaturaId);
                    } else {
                        dtoResult.setNombreAsignatura("Sin Asignatura");
                    }
                    return dtoResult;
                })
                .collect(Collectors.toList());
    }
}
