package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.DTO.ComentarioIncidenciaDTO;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.MiembroProyecto;
import com.TestTrackQA.testTrack.model.RolProyecto;
import com.TestTrackQA.testTrack.DTO.HistorialIncidenciaDTO;

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
    Incidencia actualizarAsignacionEnProyecto(Long codIncidencia, Long proyectoId, Long usuarioId,
                                               String correoUsuario);
    List<MiembroProyecto> traerMiembrosDelProyecto(Long proyectoId);
    RolProyecto obtenerRolUsuarioEnProyecto(String correoUsuario, Long proyectoId);
    List<HistorialIncidenciaDTO> traerHistorial(Long codIncidencia, String correoUsuario);
    ComentarioIncidenciaDTO agregarComentario(Long codIncidencia, String contenido, String correoUsuario);
    List<ComentarioIncidenciaDTO> traerComentarios(Long codIncidencia, String correoUsuario);
}
