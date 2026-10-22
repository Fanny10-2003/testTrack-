package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.HistorialIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IHistorialIncidenciaRepository extends JpaRepository<HistorialIncidencia, Long> {

    List<HistorialIncidencia> findAllByIncidencia_IdIncidenciaOrderByFechaDescIdDesc(Long incidenciaId);
}
