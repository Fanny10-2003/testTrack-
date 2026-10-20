package com.TestTrackQA.testTrack.repository;

import com.TestTrackQA.testTrack.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
}
