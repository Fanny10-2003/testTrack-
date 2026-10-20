package com.TestTrackQA.testTrack.DTO;

import com.TestTrackQA.testTrack.model.Proyecto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ProyectoDTO {

    private Long idProyecto;

    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @Size(max = 500, message = "La URL no puede superar los 500 caracteres")
    private String urlBase;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private Long administradorId;
    private String administradorNombre;
    private boolean eliminado;
    private long totalIncidencias;
    private long criticasAbiertas;
    private long incidenciasAbiertas;
    private long incidenciasCerradas;
    private String rolUsuario;

    public static ProyectoDTO fromEntity(Proyecto proyecto) {
        ProyectoDTO dto = new ProyectoDTO();
        dto.setIdProyecto(proyecto.getIdProyecto());
        dto.setNombre(proyecto.getNombre());
        dto.setDescripcion(proyecto.getDescripcion());
        dto.setUrlBase(proyecto.getUrlBase());
        dto.setFechaCreacion(proyecto.getFecha_creacion());
        dto.setFechaModificacion(proyecto.getFecha_modificacion());
        dto.setEliminado(proyecto.isEliminado());
        if (proyecto.getAdministrador() != null) {
            dto.setAdministradorId(proyecto.getAdministrador().getIdUsuario());
            dto.setAdministradorNombre(proyecto.getAdministrador().getNombre());
        }
        return dto;
    }
}
