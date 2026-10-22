package com.TestTrackQA.testTrack.DTO;

import com.TestTrackQA.testTrack.model.ComentarioIncidencia;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComentarioIncidenciaDTO {

    private Long id;
    private Long incidenciaId;
    private Long usuarioId;
    private String usuarioNombre;
    private String contenido;
    private LocalDateTime fechaCreacion;

    public static ComentarioIncidenciaDTO fromEntity(ComentarioIncidencia comentario) {
        ComentarioIncidenciaDTO dto = new ComentarioIncidenciaDTO();
        dto.setId(comentario.getId());
        dto.setIncidenciaId(comentario.getIncidencia().getIdIncidencia());
        dto.setUsuarioId(comentario.getUsuario().getIdUsuario());
        dto.setUsuarioNombre(comentario.getUsuario().getNombre());
        dto.setContenido(comentario.getContenido());
        dto.setFechaCreacion(comentario.getFechaCreacion());
        return dto;
    }
}
