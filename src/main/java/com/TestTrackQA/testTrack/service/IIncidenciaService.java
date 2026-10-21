package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;

import java.util.List;

public interface IIncidenciaService {

    List<Incidencia> traerIncidencias();
    List<Incidencia> traerIncidenciasPorProyecto(Long proyectoId);
    List<Incidencia> traerIncidenciasPorProyectos(List<Long> proyectoIds);
    Incidencia crearIncidencia(Incidencia incidencia, Long proyectoId, String correoUsuario);
    Incidencia buscarIncidencia (Long codIncidencia);
    Incidencia buscarIncidenciaEnProyecto(Long codIncidencia, Long proyectoId);
    Incidencia editarIncidenciaEnProyecto(Long codIncidencia, Incidencia cambios,
                                          Long proyectoId, String correoUsuario);
    Incidencia cambiarEstadoEnProyecto(Long codIncidencia, Long proyectoId,
                                       EstadoIncidencia nuevoEstado, String correoUsuario);
    Incidencia tomarIncidencia(Long codIncidencia, String correoUsuario);
    Incidencia liberarIncidencia(Long codIncidencia, String correoUsuario);
}
