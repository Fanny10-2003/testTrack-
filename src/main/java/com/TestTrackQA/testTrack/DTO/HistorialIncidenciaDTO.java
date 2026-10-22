package com.TestTrackQA.testTrack.DTO;

import com.TestTrackQA.testTrack.model.HistorialIncidencia;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class HistorialIncidenciaDTO {

    private Long id;
    private String usuarioNombre;
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;
    private LocalDateTime fecha;
    private String mensaje;
    private String tiempoRelativo;

    public static HistorialIncidenciaDTO fromEntity(HistorialIncidencia historial) {
        HistorialIncidenciaDTO dto = new HistorialIncidenciaDTO();
        dto.setId(historial.getId());
        dto.setUsuarioNombre(historial.getUsuario().getNombre());
        dto.setCampoModificado(historial.getCampoModificado());
        dto.setValorAnterior(historial.getValorAnterior());
        dto.setValorNuevo(historial.getValorNuevo());
        dto.setFecha(historial.getFecha());
        dto.setMensaje(crearMensaje(dto));
        dto.setTiempoRelativo(tiempoRelativo(historial.getFecha()));
        return dto;
    }

    private static String crearMensaje(HistorialIncidenciaDTO historial) {
        if ("incidencia".equals(historial.getCampoModificado())) {
            return historial.getUsuarioNombre() + " creó la incidencia";
        }
        return historial.getUsuarioNombre() + " cambió " + historial.getCampoModificado()
                + " de " + mostrarValor(historial.getValorAnterior())
                + " a " + mostrarValor(historial.getValorNuevo());
    }

    private static String mostrarValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return "Sin asignar";
        }
        return switch (valor) {
            case "ABIERTO" -> "Abierto";
            case "CERRADO" -> "Cerrado";
            case "EN_PROGRESO" -> "En progreso";
            case "RESUELTO" -> "Resuelto";
            default -> valor;
        };
    }

    private static String tiempoRelativo(LocalDateTime fecha) {
        Duration diferencia = Duration.between(fecha, LocalDateTime.now());
        long segundos = Math.max(0, diferencia.getSeconds());
        if (segundos < 60) {
            return "hace unos segundos";
        }
        long minutos = segundos / 60;
        if (minutos < 60) {
            return minutos == 1 ? "hace 1 minuto" : "hace " + minutos + " minutos";
        }
        long horas = minutos / 60;
        if (horas < 24) {
            return horas == 1 ? "hace 1 hora" : "hace " + horas + " horas";
        }
        long dias = horas / 24;
        return dias == 1 ? "hace 1 día" : "hace " + dias + " días";
    }
}
