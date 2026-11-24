package com.microservicio.alumnos;

import com.microservicio.alumnos.dto.dto;
import com.microservicio.alumnos.repository.repositoryAlumno;
import com.microservicio.alumnos.service.servicio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@EnableFeignClients
@SpringBootApplication
public class AlumnosApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlumnosApplication.class, args);
    }
    // Cargar datos iniciales para que sea mas facil
    @Bean
    CommandLineRunner seedData(repositoryAlumno repo, servicio srv) {
        return args -> {
            if (repo.count() == 0) {
                String[][] alumnosData = {
                    {"Erik", "2023171006", "false"},
                    {"Valeria", "2023171007", "true"},
                    {"Karen", "2023171008", "true"},
                    {"Emilio", "2023171009", "true"},
                    {"Saul", "2023171010", "true"},
                    {"Luis", "2023171011", "true"}
                };

                for (String[] alum : alumnosData) {
                    dto nuevo = new dto();
                    nuevo.setNombreAlumno(alum[0]);
                    nuevo.setMatricula(alum[1]);
                    nuevo.setEstadoAlumno(Boolean.parseBoolean(alum[2]));
                    nuevo.setEntityAsignaturaId(null);
                    srv.crear(nuevo);
                }
            }
        };
    }
}
