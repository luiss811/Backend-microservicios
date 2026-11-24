package com.maestros.maestros.repository;

import com.maestros.maestros.entity.MaestroAsignatura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MaestroAsignaturaRepository extends JpaRepository<MaestroAsignatura, Long> {
    List<MaestroAsignatura> findByAsignaturaIdIn(Collection<Long> asignaturaIds);
}
