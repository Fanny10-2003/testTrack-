package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;

import java.util.List;

public interface IIncidenciaService {

    List<Incidencia> traerIncidencias();
    Incidencia crearIncidencia(Incidencia incidencia, Long proyectoId, Long usuarioId);
    Incidencia buscarIncidencia (Long codIncidencia);
    Incidencia editarIncidencia (Long codIncidencia, Incidencia incidencia, Long proyectoId, Long usuarioId);
    Incidencia cambiarEstado(Long codIncidencia, EstadoIncidencia nuevoEstado);
}
