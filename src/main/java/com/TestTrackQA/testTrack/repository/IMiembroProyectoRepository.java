package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.MiembroProyecto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IMiembroProyectoRepository extends JpaRepository<MiembroProyecto, Long> {

    @EntityGraph(attributePaths = {"proyecto", "proyecto.administrador"})
    List<MiembroProyecto> findAllByUsuario_CorreoIgnoreCaseAndProyecto_EliminadoFalse(String correo);

    @EntityGraph(attributePaths = {"proyecto", "proyecto.administrador"})
    Optional<MiembroProyecto> findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
            String correo, Long idProyecto);
}
