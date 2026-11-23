package com.microservicio.serviciosocial.service;

import com.microservicio.serviciosocial.client.ClientFeignAlumno;
import com.microservicio.serviciosocial.dto.AlumnoDto;
import com.microservicio.serviciosocial.dto.ServicioSocialDto;
import com.microservicio.serviciosocial.dto.ServicioSocialListDto;
import com.microservicio.serviciosocial.dto.StatusServicioSocialDto;
import com.microservicio.serviciosocial.entity.ServicioSocial;
import com.microservicio.serviciosocial.repository.ServicioSocialRepository;
import feign.FeignException;
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
public class ServicioSocialService {

    @Autowired
    private ServicioSocialRepository repository;

    @Autowired
    private ClientFeignAlumno clientFeignAlumno;

    public ServicioSocialDto convertirDTO(ServicioSocial entity) {
        ServicioSocialDto dto = new ServicioSocialDto();
        dto.setId(entity.getId());
        dto.setAlumnoId(entity.getAlumnoId());
        dto.setInstitucion(entity.getInstitucion());
        dto.setPrograma(entity.getPrograma());
        dto.setHorasAcumuladas(entity.getHorasAcumuladas());
        dto.setHorasTotales(entity.getHorasTotales());
        dto.setEstado(entity.getEstado());
        dto.setFechaInicio(entity.getFechaInicio());
        return dto;
    }

    private AlumnoDto validarAlumno(Long alumnoId) {
        if (alumnoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del alumno es obligatorio.");
        }
        try {
            AlumnoDto alumno = clientFeignAlumno.buscarPorId(alumnoId);
            if (alumno == null || alumno.getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El Alumno con ID " + alumnoId + " no existe en el microservicio de Alumnos.");
            }
            return alumno;
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El Alumno con ID " + alumnoId + " no existe en el microservicio de Alumnos.");
        } catch (FeignException.InternalServerError e) {
            String body = e.contentUTF8();
            if (body != null && (body.contains("Alumno no encontrado") || body.contains("no encontrado"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El Alumno con ID " + alumnoId + " no existe en el microservicio de Alumnos.");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error interno en el microservicio de Alumnos al verificar el alumno: " + e.getMessage());
        } catch (FeignException fe) {
            if (fe.status() == 404 || fe.status() == 400) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El Alumno con ID " + alumnoId + " no existe en el microservicio de Alumnos.");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Error al comunicarse con el microservicio de Alumnos (HTTP " + fe.status() + "): " + fe.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo contactar al microservicio de Alumnos para verificar el alumno: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ServicioSocialDto> listar() {
        return repository.findAll()
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServicioSocialDto buscar(Long id) {
        ServicioSocial registro = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de Servicio Social no encontrado."));
        return convertirDTO(registro);
    }

    @Transactional
    public ServicioSocialDto crear(ServicioSocialDto datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del Servicio Social no pueden ser nulos.");
        }
        if (datos.getAlumnoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del alumno es obligatorio.");
        }

        // Regla de negocio: Un alumno no puede tener más de un servicio social
        List<ServicioSocial> existentes = repository.findByAlumnoId(datos.getAlumnoId());
        if (!existentes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El alumno con ID " + datos.getAlumnoId() + " ya cuenta con un Servicio Social registrado. No se permite tener más de uno.");
        }

        validarAlumno(datos.getAlumnoId());

        ServicioSocial nuevo = new ServicioSocial();
        nuevo.setAlumnoId(datos.getAlumnoId());
        nuevo.setInstitucion(datos.getInstitucion() != null && !datos.getInstitucion().trim().isEmpty()
                ? datos.getInstitucion().trim() : "Sin institución asignada");
        nuevo.setPrograma(datos.getPrograma() != null && !datos.getPrograma().trim().isEmpty()
                ? datos.getPrograma().trim() : "Servicio Social General");

        int acumuladas = datos.getHorasAcumuladas() != null ? Math.max(0, datos.getHorasAcumuladas()) : 0;
        int totales = (datos.getHorasTotales() != null && datos.getHorasTotales() > 0) ? datos.getHorasTotales() : 480;
        nuevo.setHorasAcumuladas(acumuladas);
        nuevo.setHorasTotales(totales);

        // Determinación de los 3 estatus (NO_LIBERADO, EN_PROCESO, LIBERADO)
        nuevo.setEstado(determinarEstado(acumuladas, totales, datos.getEstado()));

        nuevo.setFechaInicio(datos.getFechaInicio() != null && !datos.getFechaInicio().trim().isEmpty()
                ? datos.getFechaInicio().trim() : "Pendiente");

        ServicioSocial guardado = repository.save(nuevo);
        return convertirDTO(guardado);
    }

    @Transactional
    public ServicioSocialDto actualizar(Long id, ServicioSocialDto datos) {
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos para actualizar no pueden ser nulos.");
        }
        ServicioSocial registro = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de Servicio Social no encontrado."));

        if (datos.getAlumnoId() != null && !datos.getAlumnoId().equals(registro.getAlumnoId())) {
            // Regla de negocio al reasignar: no permitir duplicados
            List<ServicioSocial> existentes = repository.findByAlumnoId(datos.getAlumnoId());
            if (!existentes.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El alumno con ID " + datos.getAlumnoId() + " ya cuenta con otro Servicio Social registrado.");
            }

            validarAlumno(datos.getAlumnoId());
            registro.setAlumnoId(datos.getAlumnoId());
        }

        if (datos.getInstitucion() != null) {
            registro.setInstitucion(datos.getInstitucion().trim());
        }
        if (datos.getPrograma() != null) {
            registro.setPrograma(datos.getPrograma().trim());
        }
        if (datos.getHorasTotales() != null && datos.getHorasTotales() > 0) {
            registro.setHorasTotales(datos.getHorasTotales());
        }
        if (datos.getHorasAcumuladas() != null) {
            registro.setHorasAcumuladas(Math.max(0, datos.getHorasAcumuladas()));
        }

        // Regla automática: tras editar las horas, si alcanzan o superan las horas totales -> LIBERADO
        String estadoFinal = determinarEstado(
                registro.getHorasAcumuladas(),
                registro.getHorasTotales(),
                datos.getEstado()
        );
        registro.setEstado(estadoFinal);

        if (datos.getFechaInicio() != null && !datos.getFechaInicio().trim().isEmpty()) {
            registro.setFechaInicio(datos.getFechaInicio().trim());
        }

        ServicioSocial actualizado = repository.save(registro);
        return convertirDTO(actualizado);
    }

    /**
     * Determina el estatus entre los tres permitidos: NO_LIBERADO, EN_PROCESO, LIBERADO.
     * Regla clave: Si horasAcumuladas >= horasTotales -> pasa automáticamente a LIBERADO.
     */
    private String determinarEstado(Integer horasAcumuladas, Integer horasTotales, String estadoDeseado) {
        int acumuladas = (horasAcumuladas != null) ? Math.max(0, horasAcumuladas) : 0;
        int totales = (horasTotales != null && horasTotales > 0) ? horasTotales : 480;

        // Si cumplió o superó las horas requeridas -> LIBERADO automáticamente
        if (acumuladas >= totales) {
            return "LIBERADO";
        }

        if (estadoDeseado != null && !estadoDeseado.trim().isEmpty()) {
            String est = estadoDeseado.trim().toUpperCase().replace("_", " ");
            if ("LIBERADO".equals(est)) {
                // No puede estar liberado si no ha completado las horas
                return acumuladas > 0 ? "EN PROCESO" : "NO LIBERADO";
            }
            if ("NO LIBERADO".equals(est) || "EN PROCESO".equals(est)) {
                return est;
            }
        }

        // Si no se especificó un estatus válido:
        return acumuladas > 0 ? "EN PROCESO" : "NO LIBERADO";
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de Servicio Social no encontrado.");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ServicioSocialListDto> listarConAlumnos() {
        List<ServicioSocial> registros = repository.findAll();

        Map<Long, AlumnoDto> alumnoMap = new HashMap<>();
        try {
            List<AlumnoDto> alumnos = clientFeignAlumno.listarTodos();
            if (alumnos != null) {
                for (AlumnoDto a : alumnos) {
                    if (a != null && a.getId() != null) {
                        alumnoMap.put(a.getId(), a);
                    }
                }
            }
        } catch (Exception e) {
            // En caso de que el microservicio de alumnos no responda en ese momento
        }

        return registros.stream().map(s -> {
            ServicioSocialListDto dto = new ServicioSocialListDto();
            dto.setId(s.getId());
            dto.setAlumnoId(s.getAlumnoId());
            dto.setInstitucion(s.getInstitucion());
            dto.setPrograma(s.getPrograma());
            dto.setHorasAcumuladas(s.getHorasAcumuladas());
            dto.setHorasTotales(s.getHorasTotales());
            dto.setEstado(s.getEstado());
            dto.setFechaInicio(s.getFechaInicio());

            AlumnoDto al = alumnoMap.get(s.getAlumnoId());
            if (al != null) {
                dto.setNombreAlumno(al.getNombreAlumno());
                dto.setMatricula(al.getMatricula());
            } else {
                dto.setNombreAlumno("Alumno #" + s.getAlumnoId());
                dto.setMatricula("N/A");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Consulta el status detallado del Servicio Social de un alumno (vía OpenFeign + BD local)
     */
    @Transactional(readOnly = true)
    public StatusServicioSocialDto obtenerStatusPorAlumno(Long alumnoId) {
        AlumnoDto alumno = null;
        try {
            alumno = validarAlumno(alumnoId);
        } catch (Exception e) {
            // Fallback si Alumnos no está disponible en este momento
        }

        StatusServicioSocialDto statusDto = new StatusServicioSocialDto();
        statusDto.setAlumnoId(alumnoId);
        statusDto.setNombreAlumno(alumno != null ? alumno.getNombreAlumno() : "Alumno #" + alumnoId);
        statusDto.setMatricula(alumno != null ? alumno.getMatricula() : "N/A");

        List<ServicioSocial> registros = repository.findByAlumnoId(alumnoId);
        if (registros.isEmpty()) {
            statusDto.setTieneServicio(false);
            statusDto.setEstado("SIN_SERVICIO");
            statusDto.setHorasAcumuladas(0);
            statusDto.setHorasTotales(480);
            statusDto.setPorcentajeAvance(0.0);
            statusDto.setMensaje("El alumno aún no ha iniciado ni tiene asignado un Servicio Social.");
            return statusDto;
        }

        ServicioSocial s = registros.get(0);
        statusDto.setTieneServicio(true);
        statusDto.setServicioSocialId(s.getId());
        statusDto.setInstitucion(s.getInstitucion());
        statusDto.setPrograma(s.getPrograma());
        statusDto.setHorasAcumuladas(s.getHorasAcumuladas());
        statusDto.setHorasTotales(s.getHorasTotales());
        statusDto.setEstado(s.getEstado());
        statusDto.setFechaInicio(s.getFechaInicio());

        int acumuladas = s.getHorasAcumuladas() != null ? s.getHorasAcumuladas() : 0;
        int totales = (s.getHorasTotales() != null && s.getHorasTotales() > 0) ? s.getHorasTotales() : 480;
        double porcentaje = Math.min(100.0, Math.round(((double) acumuladas / totales) * 10000.0) / 100.0);
        statusDto.setPorcentajeAvance(porcentaje);

        if ("LIBERADO".equalsIgnoreCase(s.getEstado()) || acumuladas >= totales) {
            statusDto.setEstado("LIBERADO");
            statusDto.setMensaje("El alumno ha completado satisfactoriamente su Servicio Social (Liberado).");
        } else if ((s.getEstado() != null && s.getEstado().toUpperCase().contains("NO")) || acumuladas == 0) {
            int restantes = Math.max(0, totales - acumuladas);
            statusDto.setEstado("NO LIBERADO");
            statusDto.setMensaje("Servicio Social no liberado. Horas pendientes para liberar: " + restantes + " horas.");
        } else {
            int restantes = Math.max(0, totales - acumuladas);
            statusDto.setEstado("EN PROCESO");
            statusDto.setMensaje("Servicio Social en proceso. Faltan " + restantes + " horas para completar el requisito.");
        }

        return statusDto;
    }

    /**
     * Siembra datos iniciales de prueba en la base de datos de Servicio Social.
     */
    @Transactional
    public List<ServicioSocialDto> insertarDatosIniciales() {
        List<ServicioSocialDto> resultado = new ArrayList<>();

        if (repository.count() > 0) {
            return repository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
        }

        Object[][] seeds = {
            {1L, "DIF Municipal", "Apoyo Comunitario Digital", 120, 480, "EN_PROCESO", "2026-02-01"},
            {2L, "Hospital General", "Digitalización de Archivos Clínicos", 480, 480, "LIBERADO", "2025-08-15"},
            {3L, "Secretaría de Innovación", "Desarrollo de Portales Web Ciudadanos", 240, 480, "EN_PROCESO", "2026-01-10"}
        };

        for (Object[] seed : seeds) {
            Long alumnoId = (Long) seed[0];
            try {
                validarAlumno(alumnoId);
            } catch (Exception ignored) {
                // Si Alumnos no está disponible en este momento, se permite sembrar para pruebas
            }

            ServicioSocial s = new ServicioSocial();
            s.setAlumnoId(alumnoId);
            s.setInstitucion((String) seed[1]);
            s.setPrograma((String) seed[2]);
            s.setHorasAcumuladas((Integer) seed[3]);
            s.setHorasTotales((Integer) seed[4]);
            s.setEstado((String) seed[5]);
            s.setFechaInicio((String) seed[6]);
            resultado.add(convertirDTO(repository.save(s)));
        }

        return resultado;
    }
}
