package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.MiembroProyecto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IMiembroProyectoRepository extends JpaRepository<MiembroProyecto, Long> {
}
