package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.Incidencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIncidenciaRepository extends JpaRepository<Incidencia, Long> {

    @EntityGraph(attributePaths = {"proyecto", "usuario"})
    @Query("select i from Incidencia i")
    List<Incidencia> findAllWithProyectoAndUsuario();

    @EntityGraph(attributePaths = {"proyecto", "usuario"})
    @Query("select i from Incidencia i where i.idIncidencia = :id")
    Optional<Incidencia> findWithProyectoAndUsuarioById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"proyecto", "usuario"})
    List<Incidencia> findAllByProyecto_IdProyecto(Long proyectoId);

    @EntityGraph(attributePaths = {"proyecto"})
    List<Incidencia> findAllByProyecto_IdProyectoIn(List<Long> proyectoIds);

    @EntityGraph(attributePaths = {"proyecto", "usuario"})
    Optional<Incidencia> findByIdIncidenciaAndProyecto_IdProyecto(Long incidenciaId, Long proyectoId);
}
