package com.microservicio.alumnos.repository;

import com.microservicio.alumnos.entity.EntityAlumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface repositoryAlumno extends JpaRepository<EntityAlumno, Long> {

    Optional<EntityAlumno> findByMatricula(String matricula);

    boolean existsByMatricula(String matricula);
}
