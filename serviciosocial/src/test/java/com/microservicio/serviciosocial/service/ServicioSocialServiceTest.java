package com.microservicio.serviciosocial.service;

import com.microservicio.serviciosocial.client.ClientFeignAlumno;
import com.microservicio.serviciosocial.dto.AlumnoDto;
import com.microservicio.serviciosocial.dto.ServicioSocialDto;
import com.microservicio.serviciosocial.dto.StatusServicioSocialDto;
import com.microservicio.serviciosocial.entity.ServicioSocial;
import com.microservicio.serviciosocial.repository.ServicioSocialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioSocialServiceTest {

    @Mock
    private ServicioSocialRepository repository;

    @Mock
    private ClientFeignAlumno clientFeignAlumno;

    @InjectMocks
    private ServicioSocialService service;

    private AlumnoDto alumnoPrueba;

    @BeforeEach
    void setUp() {
        alumnoPrueba = new AlumnoDto();
        alumnoPrueba.setId(1L);
        alumnoPrueba.setNombreAlumno("Erik");
        alumnoPrueba.setMatricula("2023171006");
        alumnoPrueba.setEstadoAlumno(true);
    }

    @Test
    @DisplayName("Debe crear un servicio social exitosamente cuando el alumno existe y no tiene servicio previo")
    void testCrearExitoso() {
        when(clientFeignAlumno.buscarPorId(1L)).thenReturn(alumnoPrueba);
        when(repository.findByAlumnoId(1L)).thenReturn(Collections.emptyList());

        ServicioSocial entityGuardada = new ServicioSocial();
        entityGuardada.setId(10L);
        entityGuardada.setAlumnoId(1L);
        entityGuardada.setInstitucion("DIF");
        entityGuardada.setPrograma("Salud");
        entityGuardada.setHorasAcumuladas(100);
        entityGuardada.setHorasTotales(480);
        entityGuardada.setEstado("EN_PROCESO");
        entityGuardada.setFechaInicio("2026-01-01");

        when(repository.save(any(ServicioSocial.class))).thenReturn(entityGuardada);

        ServicioSocialDto datos = new ServicioSocialDto();
        datos.setAlumnoId(1L);
        datos.setInstitucion("DIF");
        datos.setPrograma("Salud");
        datos.setHorasAcumuladas(100);
        datos.setHorasTotales(480);

        ServicioSocialDto resultado = service.crear(datos);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("EN_PROCESO", resultado.getEstado());
        assertEquals(100, resultado.getHorasAcumuladas());
        verify(repository, times(1)).save(any(ServicioSocial.class));
    }

    @Test
    @DisplayName("Debe rechazar la creación si el alumno ya tiene un servicio social registrado")
    void testCrearRechazaDuplicado() {
        when(clientFeignAlumno.buscarPorId(1L)).thenReturn(alumnoPrueba);

        ServicioSocial existente = new ServicioSocial();
        existente.setId(5L);
        existente.setAlumnoId(1L);
        when(repository.findByAlumnoId(1L)).thenReturn(List.of(existente));

        ServicioSocialDto datos = new ServicioSocialDto();
        datos.setAlumnoId(1L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.crear(datos));
        assertTrue(ex.getReason().contains("ya cuenta con un Servicio Social registrado"));
        verify(repository, never()).save(any(ServicioSocial.class));
    }

    @Test
    @DisplayName("Debe marcar como LIBERADO automáticamente si las horas acumuladas cubren las totales")
    void testLiberacionAutomaticaAlCumplirHoras() {
        when(clientFeignAlumno.buscarPorId(1L)).thenReturn(alumnoPrueba);
        when(repository.findByAlumnoId(1L)).thenReturn(Collections.emptyList());

        when(repository.save(any(ServicioSocial.class))).thenAnswer(invocation -> {
            ServicioSocial arg = invocation.getArgument(0);
            arg.setId(1L);
            return arg;
        });

        ServicioSocialDto datos = new ServicioSocialDto();
        datos.setAlumnoId(1L);
        datos.setInstitucion("Hospital");
        datos.setHorasAcumuladas(480);
        datos.setHorasTotales(480);

        ServicioSocialDto resultado = service.crear(datos);

        assertEquals("LIBERADO", resultado.getEstado());
    }

    @Test
    @DisplayName("Debe calcular correctamente el porcentaje de avance en obtenerStatusPorAlumno")
    void testStatusPorAlumno() {
        when(clientFeignAlumno.buscarPorId(1L)).thenReturn(alumnoPrueba);

        ServicioSocial s = new ServicioSocial();
        s.setId(20L);
        s.setAlumnoId(1L);
        s.setInstitucion("Cruz Roja");
        s.setPrograma("Primeros Auxilios");
        s.setHorasAcumuladas(240);
        s.setHorasTotales(480);
        s.setEstado("EN_PROCESO");

        when(repository.findByAlumnoId(1L)).thenReturn(List.of(s));

        StatusServicioSocialDto status = service.obtenerStatusPorAlumno(1L);

        assertTrue(status.getTieneServicio());
        assertEquals(50.0, status.getPorcentajeAvance());
        assertEquals("EN_PROCESO", status.getEstado());
        assertTrue(status.getMensaje().contains("Faltan 240 horas"));
    }

    @Test
    @DisplayName("Debe reportar SIN_SERVICIO cuando el alumno no tiene registros")
    void testStatusSinServicio() {
        when(clientFeignAlumno.buscarPorId(1L)).thenReturn(alumnoPrueba);
        when(repository.findByAlumnoId(1L)).thenReturn(Collections.emptyList());

        StatusServicioSocialDto status = service.obtenerStatusPorAlumno(1L);

        assertFalse(status.getTieneServicio());
        assertEquals("SIN_SERVICIO", status.getEstado());
        assertEquals(0.0, status.getPorcentajeAvance());
    }
}
