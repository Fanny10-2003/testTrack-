package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Usuario;

import java.util.List;
import java.util.Map;

public interface IUsuarioService {

    Usuario buscarPorCorreo(String correo);
    String nombrePorCorreo(String correo);
    boolean existePorCorreo(String correo);
    List<String> fotosPerfilDisponibles();
    Map<String, String> nombresFotosPerfil();
    boolean fotoPerfilPermitida(String nombreArchivo);
    Usuario guardar(Usuario usuario);
}
