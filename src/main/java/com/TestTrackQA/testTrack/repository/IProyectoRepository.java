package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.Proyecto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IProyectoRepository extends JpaRepository<Proyecto, Long> {

    @EntityGraph(attributePaths = "administrador")
    List<Proyecto> findAllByAdministrador_CorreoIgnoreCaseAndEliminadoFalse(String correo);

    @EntityGraph(attributePaths = "administrador")
    Optional<Proyecto> findByIdProyectoAndAdministrador_CorreoIgnoreCaseAndEliminadoFalse(
            Long idProyecto, String correo);
}
