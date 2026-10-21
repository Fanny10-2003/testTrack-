package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.DTO.ProyectoDTO;

import java.util.List;

public interface IProyectoService {

    List<ProyectoDTO> listarDelAdministrador(String correo);
    String nombreAdministrador(String correo);
    Long crear(ProyectoDTO datos, String correoAdministrador);
    ProyectoDTO buscarPropio(Long idProyecto, String correoAdministrador);
    void editar(Long idProyecto, ProyectoDTO datos, String correoAdministrador);
    void eliminar(Long idProyecto, String correoAdministrador);
}
