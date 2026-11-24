package com.microservicio.serviciosocial.repository;

import com.microservicio.serviciosocial.entity.ServicioSocial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicioSocialRepository extends JpaRepository<ServicioSocial, Long> {
    List<ServicioSocial> findByAlumnoId(Long alumnoId);
}
