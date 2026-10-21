package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;

import java.util.List;

public interface IIncidenciaService {

    List<Incidencia> traerIncidencias();
    List<Incidencia> traerIncidenciasPorProyecto(Long proyectoId);
    List<Incidencia> traerIncidenciasPorProyectos(List<Long> proyectoIds);
    Incidencia crearIncidencia(Incidencia incidencia, Long proyectoId, Long usuarioId);
    Incidencia buscarIncidencia (Long codIncidencia);
    Incidencia buscarIncidenciaEnProyecto(Long codIncidencia, Long proyectoId);
    Incidencia editarIncidencia (Long codIncidencia, Incidencia incidencia, Long proyectoId, Long usuarioId);
    Incidencia editarIncidenciaEnProyecto(Long codIncidencia, Incidencia cambios, Long proyectoId);
    Incidencia cambiarEstado(Long codIncidencia, EstadoIncidencia nuevoEstado);
    Incidencia cambiarEstadoEnProyecto(Long codIncidencia, Long proyectoId, EstadoIncidencia nuevoEstado);
}
