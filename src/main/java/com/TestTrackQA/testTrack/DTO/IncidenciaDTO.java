package com.TestTrackQA.testTrack.DTO;

import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IncidenciaDTO {

    private Long idIncidencia;

    @Size(max = 24, message = "El código no puede superar los 24 caracteres")
    private String codigoIncidencia;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 120, message = "El título no puede superar los 120 caracteres")
    private String titulo;

    @NotBlank(message = "El módulo es obligatorio")
    @Size(max = 80, message = "El módulo no puede superar los 80 caracteres")
    private String modulo;

    @Size(max = 60, message = "El entorno o versión no puede superar los 60 caracteres")
    private String entornoVersion;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String descripcion;

    @Size(max = 1000, message = "Los pasos no pueden superar los 1000 caracteres")
    private String pasos;

    @Size(max = 500, message = "El resultado esperado no puede superar los 500 caracteres")
    private String resultadoEsperado;

    @Size(max = 500, message = "El resultado actual no puede superar los 500 caracteres")
    private String resultadoActual;

    @NotBlank(message = "La severidad es obligatoria")
    @Pattern(regexp = "BAJA|MEDIA|ALTA|CRITICA", message = "La severidad debe ser BAJA, MEDIA, ALTA o CRITICA")
    @Size(max = 7, message = "La severidad no puede superar los 7 caracteres")
    private String severidad;

    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
    private String comentario;

    @Size(max = 500, message = "La evidencia no puede superar los 500 caracteres")
    private String evidencia;

    private EstadoIncidencia estado;

    public Incidencia toEntity() {
        Incidencia incidencia = new Incidencia();
        incidencia.setIdIncidencia(idIncidencia);
        incidencia.setTitulo(titulo);
        incidencia.setModulo(modulo);
        incidencia.setEntornoVersion(entornoVersion);
        incidencia.setDescripcion(descripcion);
        incidencia.setPasos(pasos);
        incidencia.setResultadoEsperado(resultadoEsperado);
        incidencia.setResultadoActual(resultadoActual);
        incidencia.setSeveridad(severidad);
        incidencia.setComentario(comentario);
        incidencia.setEvidencia(evidencia);
        return incidencia;
    }

    public static IncidenciaDTO fromEntity(Incidencia incidencia) {
        IncidenciaDTO dto = new IncidenciaDTO();
        dto.setIdIncidencia(incidencia.getIdIncidencia());
        dto.setCodigoIncidencia(incidencia.getCodigoIncidencia());
        dto.setEstado(incidencia.getEstado());
        dto.setTitulo(incidencia.getTitulo());
        dto.setModulo(incidencia.getModulo());
        dto.setEntornoVersion(incidencia.getEntornoVersion());
        dto.setDescripcion(incidencia.getDescripcion());
        dto.setPasos(incidencia.getPasos());
        dto.setResultadoEsperado(incidencia.getResultadoEsperado());
        dto.setResultadoActual(incidencia.getResultadoActual());
        dto.setSeveridad(incidencia.getSeveridad());
        dto.setComentario(incidencia.getComentario());
        dto.setEvidencia(incidencia.getEvidencia());
        return dto;
    }
}
