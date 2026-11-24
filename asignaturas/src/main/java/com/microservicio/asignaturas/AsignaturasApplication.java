package com.microservicio.asignaturas;

import com.microservicio.asignaturas.entity.Asignatura;
import com.microservicio.asignaturas.repository.AsignaturaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableFeignClients
@SpringBootApplication
public class AsignaturasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsignaturasApplication.class, args);
    }

    @Bean
    CommandLineRunner seedAsignaturas(AsignaturaRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Asignatura(null, "Desarrollo Web Profesional", "Desarrollo de aplicaciones web frontend y backend", true, 1L));
                repo.save(new Asignatura(null, "Bases de Datos Avanzadas", "Modelado relacional, índices y transacciones", true, 1L));
                repo.save(new Asignatura(null, "Arquitectura de Software", "Microservicios, patrones y cloud computing", true, 1L));
            }
        };
    }
}
