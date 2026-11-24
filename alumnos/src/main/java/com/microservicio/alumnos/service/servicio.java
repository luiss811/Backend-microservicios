package com.microservicio.alumnos.service;

import com.microservicio.alumnos.client.ClientFeignAsignatura;
import com.microservicio.alumnos.dto.EntityAlumnoDtoList;
import com.microservicio.alumnos.dto.EntityAsignaturaDto;
import com.microservicio.alumnos.dto.dto;
import com.microservicio.alumnos.entity.EntityAlumno;
import com.microservicio.alumnos.entity.EntityAlumnoAsignatura;
import com.microservicio.alumnos.repository.repositoryAlumno;
import feign.FeignException;
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

    private final repositoryAlumno repositoryAlumno;
    private final ClientFeignAsignatura clientFeignAsignatura;

    public servicio(repositoryAlumno repositoryAlumno, ClientFeignAsignatura clientFeignAsignatura) {
        this.repositoryAlumno = repositoryAlumno;
        this.clientFeignAsignatura = clientFeignAsignatura;
    }

    public dto convertirDTO(EntityAlumno entityAlumno) {
        if (entityAlumno == null) {
            return null;
        }
        dto datos = new dto();
        datos.setId(entityAlumno.getId());
        datos.setNombreAlumno(entityAlumno.getNombreAlumno());
        datos.setMatricula(entityAlumno.getMatricula());
        datos.setEstadoAlumno(entityAlumno.getEstadoAlumno());
        datos.setEntityAsignaturaId(entityAlumno.obtenerAsignaturaId());
        return datos;
    }

    /**
     * Valida con el microservicio de Asignaturas (OpenFeign) que la asignatura exista.
     * Si entityAsignaturaId es null, la relación es opcional.
     */
    public void validarEntityAsignatura(Long entityAsignaturaId) {
        if (entityAsignaturaId == null) {
            return;
        }
        try {
            List<EntityAsignaturaDto> encontrados =
                    clientFeignAsignatura.obtenerDTOsDelAsignatura(List.of(entityAsignaturaId));

            boolean existe = encontrados != null && encontrados.stream()
                    .anyMatch(a -> a != null && entityAsignaturaId.equals(a.getId()));

            if (!existe) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La Asignatura #" + entityAsignaturaId + " no existe en el microservicio de Asignaturas"
                );
            }
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La Asignatura #" + entityAsignaturaId + " no existe en el microservicio de Asignaturas"
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible contactar al microservicio de Asignaturas para validar la asignatura #" + entityAsignaturaId
            );
        }
    }

    private void asignarEntityAsignatura(EntityAlumno entityAlumno, Long entityAsignaturaId) {
        entityAlumno.asignarAsignatura(entityAsignaturaId);
    }

    private void validarDatos(dto datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del alumno no pueden ser nulos");
        }
        String nombre = datos.getNombreAlumno() != null ? datos.getNombreAlumno().trim() : null;
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del alumno es obligatorio");
        }
        if (nombre.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede superar los 100 caracteres");
        }

        String matricula = datos.getMatricula() != null ? datos.getMatricula().trim() : null;
        if (matricula == null || matricula.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La matrícula es obligatoria");
        }
        if (matricula.length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La matrícula no puede superar los 50 caracteres");
        }
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
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de alumno es obligatorio");
        }
        EntityAlumno registro = repositoryAlumno.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alumno #" + id + " no encontrado"
                ));
        return convertirDTO(registro);
    }

    @Transactional
    public dto crear(dto datos) {
        validarDatos(datos);
        validarEntityAsignatura(datos.getEntityAsignaturaId());

        EntityAlumno nuevo = new EntityAlumno();
        nuevo.setNombreAlumno(datos.getNombreAlumno().trim());
        nuevo.setMatricula(datos.getMatricula().trim());
        nuevo.setEstadoAlumno(datos.getEstadoAlumno() != null ? datos.getEstadoAlumno() : true);
        asignarEntityAsignatura(nuevo, datos.getEntityAsignaturaId());

        EntityAlumno guardado = repositoryAlumno.save(nuevo);
        return convertirDTO(guardado);
    }

    @Transactional
    public dto actualizar(Long id, dto datos) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de alumno es obligatorio");
        }
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de actualización no pueden ser nulos");
        }

        EntityAlumno registro = repositoryAlumno.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alumno #" + id + " no encontrado"
                ));

        if (datos.getNombreAlumno() != null) {
            String nombre = datos.getNombreAlumno().trim();
            if (nombre.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede estar vacío");
            }
            if (nombre.length() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede superar los 100 caracteres");
            }
            registro.setNombreAlumno(nombre);
        }

        if (datos.getMatricula() != null) {
            String matricula = datos.getMatricula().trim();
            if (matricula.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La matrícula no puede estar vacía");
            }
            if (matricula.length() > 50) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La matrícula no puede superar los 50 caracteres");
            }
            registro.setMatricula(matricula);
        }

        if (datos.getEstadoAlumno() != null) {
            registro.setEstadoAlumno(datos.getEstadoAlumno());
        }

        // Si se envió entityAsignaturaId (incluyendo explícitamente null para desasociar)
        if (datos.getEntityAsignaturaId() != null) {
            validarEntityAsignatura(datos.getEntityAsignaturaId());
            asignarEntityAsignatura(registro, datos.getEntityAsignaturaId());
        }

        EntityAlumno actualizado = repositoryAlumno.save(registro);
        return convertirDTO(actualizado);
    }

    @Transactional
    public dto cambiarEstado(Long id, boolean estado) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de alumno es obligatorio");
        }
        EntityAlumno registro = repositoryAlumno.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Alumno #" + id + " no encontrado"
                ));
        registro.setEstadoAlumno(estado);
        return convertirDTO(repositoryAlumno.save(registro));
    }

    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID de alumno es obligatorio");
        }
        if (!repositoryAlumno.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Alumno #" + id + " no encontrado"
            );
        }
        repositoryAlumno.deleteById(id);
    }

    /**
     * Consulta combinada de alumnos con los nombres de sus asignaturas correspondientes
     * obtenidas en batch a través de OpenFeign hacia el microservicio de Asignaturas.
     */
    @Transactional(readOnly = true)
    public List<EntityAlumnoDtoList> findAll() {
        List<EntityAlumno> datosAlumno = repositoryAlumno.findAll();

        List<Long> idsRelacionados = datosAlumno.stream()
                .map(EntityAlumno::obtenerAsignaturaId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> asignaturaMap = new HashMap<>();
        if (!idsRelacionados.isEmpty()) {
            try {
                List<EntityAsignaturaDto> listaAsignaturas =
                        clientFeignAsignatura.obtenerDTOsDelAsignatura(idsRelacionados);

                if (listaAsignaturas != null) {
                    for (EntityAsignaturaDto asig : listaAsignaturas) {
                        if (asig != null && asig.getId() != null) {
                            asignaturaMap.put(asig.getId(), asig.getNombreAsignatura());
                        }
                    }
                }
            } catch (Exception e) {
                // Si el microservicio de Asignaturas no responde o falla, mantenemos fallback
            }
        }

        return datosAlumno.stream()
                .map(alumno -> {
                    EntityAlumnoDtoList dtoResult = new EntityAlumnoDtoList();
                    dtoResult.setId(alumno.getId());
                    dtoResult.setNombreAlumno(alumno.getNombreAlumno());
                    dtoResult.setMatricula(alumno.getMatricula());
                    dtoResult.setEstadoAlumno(alumno.getEstadoAlumno());

                    Long asigId = alumno.obtenerAsignaturaId();
                    if (asigId != null) {
                        dtoResult.setEntityAsignaturaId(asigId);
                        String nombreAsig = asignaturaMap.get(asigId);
                        dtoResult.setNombreAsignatura(
                                nombreAsig != null ? nombreAsig : "Asignatura #" + asigId
                        );
                    } else {
                        dtoResult.setEntityAsignaturaId(null);
                        dtoResult.setNombreAsignatura("Sin Asignatura");
                    }
                    return dtoResult;
                })
                .collect(Collectors.toList());
    }
}
