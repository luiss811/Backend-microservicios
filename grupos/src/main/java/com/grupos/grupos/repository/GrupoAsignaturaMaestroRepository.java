package com.grupos.grupos.repository;

import com.grupos.grupos.entity.GrupoAsignaturaMaestro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoAsignaturaMaestroRepository extends JpaRepository<GrupoAsignaturaMaestro, Long> {
    List<GrupoAsignaturaMaestro> findByMaestroId(Long maestroId);
}
