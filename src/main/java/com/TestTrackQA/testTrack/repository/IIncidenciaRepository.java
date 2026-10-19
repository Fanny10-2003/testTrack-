package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IIncidenciaRepository extends JpaRepository<Incidencia, Long> {
}
