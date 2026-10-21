package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Usuario;

import java.util.List;

public interface IUsuarioService {

    Usuario buscarPorCorreo(String correo);
    String nombrePorCorreo(String correo);
    boolean existePorCorreo(String correo);
    List<String> fotosPerfilDisponibles();
    boolean fotoPerfilPermitida(String nombreArchivo);
    Usuario guardar(Usuario usuario);
}
