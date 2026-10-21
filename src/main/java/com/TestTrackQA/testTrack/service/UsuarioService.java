package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario autenticado"));
    }

    @Override
    @Transactional(readOnly = true)
    public String nombrePorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .map(Usuario::getNombre)
                .orElse(correo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCorreo(String correo) {
        return usuarioRepository.existsByCorreoIgnoreCase(correo);
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
