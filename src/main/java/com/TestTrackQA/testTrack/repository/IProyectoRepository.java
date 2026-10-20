package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IProyectoRepository extends JpaRepository<Proyecto, Long> {
}
