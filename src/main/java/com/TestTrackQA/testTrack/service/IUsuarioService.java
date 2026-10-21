package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Usuario;

public interface IUsuarioService {

    Usuario buscarPorCorreo(String correo);
    String nombrePorCorreo(String correo);
    boolean existePorCorreo(String correo);
    Usuario guardar(Usuario usuario);
}
