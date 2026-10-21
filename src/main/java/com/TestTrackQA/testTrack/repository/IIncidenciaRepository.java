package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.Incidencia;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IIncidenciaRepository extends JpaRepository<Incidencia, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Incidencia i where i.idIncidencia = :id")
    Optional<Incidencia> findByIdForUpdate(@Param("id") Long id);
}
