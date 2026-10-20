package com.TestTrackQA.testTrack.DTO;

import com.TestTrackQA.testTrack.model.Proyecto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ProyectoDTO {

    private Long idProyecto;
    private String nombre;
    private String descripcion;
    private String urlBase;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    public Proyecto toEntity() {
        Proyecto proyecto = new Proyecto();
        proyecto.setIdProyecto(idProyecto);
        proyecto.setNombre(nombre);
        proyecto.setDescripcion(descripcion);
        proyecto.setUrlBase(urlBase);
        proyecto.setFecha_creacion(fechaCreacion);
        proyecto.setFecha_modificacion(fechaModificacion);
        return proyecto;
    }

    public static ProyectoDTO fromEntity(Proyecto proyecto) {
        ProyectoDTO dto = new ProyectoDTO();
        dto.setIdProyecto(proyecto.getIdProyecto());
        dto.setNombre(proyecto.getNombre());
        dto.setDescripcion(proyecto.getDescripcion());
        dto.setUrlBase(proyecto.getUrlBase());
        dto.setFechaCreacion(proyecto.getFecha_creacion());
        dto.setFechaModificacion(proyecto.getFecha_modificacion());
        return dto;
    }
}
