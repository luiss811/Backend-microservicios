package com.maestros.maestros.repository;

import com.maestros.maestros.entity.Maestro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaestroRepository extends JpaRepository<Maestro, Long> {
}
