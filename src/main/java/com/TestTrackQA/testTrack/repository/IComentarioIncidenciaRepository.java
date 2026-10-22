package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.ComentarioIncidencia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IComentarioIncidenciaRepository extends JpaRepository<ComentarioIncidencia, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    List<ComentarioIncidencia> findAllByIncidencia_IdIncidenciaOrderByFechaCreacionAscIdAsc(Long incidenciaId);
}
