package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {
}
