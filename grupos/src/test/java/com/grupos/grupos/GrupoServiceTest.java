package com.grupos.grupos;

import com.grupos.grupos.client.ClientFeignAsignatura;
import com.grupos.grupos.client.ClientFeignMaestro;
import com.grupos.grupos.dto.AsignaturaDto;
import com.grupos.grupos.dto.GrupoDto;
import com.grupos.grupos.dto.OpcionDto;
import com.grupos.grupos.entity.Grupo;
import com.grupos.grupos.repository.GrupoAsignaturaMaestroRepository;
import com.grupos.grupos.repository.GrupoRepository;
import com.grupos.grupos.service.GrupoService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GrupoServiceTest {

    private final GrupoRepository repository = mock(GrupoRepository.class);
    private final ClientFeignAsignatura asignaturas = mock(ClientFeignAsignatura.class);
    private final ClientFeignMaestro maestros = mock(ClientFeignMaestro.class);
    private final GrupoService service = new GrupoService(repository,
            mock(GrupoAsignaturaMaestroRepository.class), asignaturas, maestros);

    GrupoServiceTest() {
        when(repository.save(any(Grupo.class))).thenAnswer(i -> i.getArgument(0));
        when(asignaturas.porIds(any())).thenReturn(List.of(new AsignaturaDto(1L, "Matematicas")));
        when(maestros.opciones(any())).thenReturn(List.of(new OpcionDto(1L, "Matematicas", 7L, "Ana")));
    }

    @Test
    void creaGrupoCuandoElMaestroImpartelaMateria() {
        GrupoDto creado = service.crear(new GrupoDto(null, "A", null,
                List.of(new GrupoDto.Asignacion(1L, 7L))));
        assertEquals(List.of(new GrupoDto.Asignacion(1L, 7L)), creado.asignaciones());
    }

    @Test
    void rechazaMaestroQueNoImpartelaMateria() {
        GrupoDto datos = new GrupoDto(null, "A", null, List.of(new GrupoDto.Asignacion(1L, 99L)));
        assertThrows(ResponseStatusException.class, () -> service.crear(datos));
        verify(repository, never()).save(any());
    }

    @Test
    void rechazaMateriaRepetidaEnElGrupo() {
        GrupoDto datos = new GrupoDto(null, "A", null,
                List.of(new GrupoDto.Asignacion(1L, 7L), new GrupoDto.Asignacion(1L, 7L)));
        assertThrows(ResponseStatusException.class, () -> service.crear(datos));
    }
}
