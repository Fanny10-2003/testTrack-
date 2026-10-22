package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioService implements IUsuarioService {

    private static final List<String> FOTOS_PERFIL = List.of(
            "Gato uno.png",
            "Gato dos.png",
            "Gato tres.png",
            "Gato cuatro.png",
            "Gato cinco.png",
            "Gato seis.png",
            "Gato siete.png",
            "Gato ocho.png",
            "Gato nueve.png");
    private static final Map<String, String> NOMBRES_FOTOS_PERFIL = Map.of(
            "Gato uno.png", "Michi commit",
            "Gato dos.png", "El gato de incógnito",
            "Gato tres.png", "Bigotes y café: depuración fina",
            "Gato cuatro.png", "Michi en modo concentración",
            "Gato cinco.png", "Tabby full-stack",
            "Gato seis.png", "Inspector Bigotes QA",
            "Gato siete.png", "Sombra del código",
            "Gato ocho.png", "Bug bajo vigilancia",
            "Gato nueve.png", "Arquitecto ronroneante");

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
    public List<String> fotosPerfilDisponibles() {
        return FOTOS_PERFIL;
    }

    @Override
    public Map<String, String> nombresFotosPerfil() {
        return NOMBRES_FOTOS_PERFIL;
    }

    @Override
    public boolean fotoPerfilPermitida(String nombreArchivo) {
        return FOTOS_PERFIL.contains(nombreArchivo);
    }

    @Override
    @Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
