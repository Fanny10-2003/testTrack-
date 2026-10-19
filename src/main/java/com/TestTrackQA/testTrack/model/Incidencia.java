package com.TestTrackQA.testTrack.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import jakarta.persistence.Column;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "incidencias")
public class Incidencia {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long idIncidencia;
@Column(unique = true, length = 24)
private String codigoIncidencia;
private String titulo;
private String modulo;
private String entornoVersion;
@Column(length = 1000)
private String descripcion;
@Column(length = 1000)
private String pasos;
@Column(length = 500)
private String resultadoEsperado;
@Column(length = 500)
private String resultadoActual;
private String severidad;
@Column(length = 500)
private String comentario;
@Column(length = 500)
private String evidencia;
@Enumerated(EnumType.STRING)
private EstadoIncidencia estado = EstadoIncidencia.ABIERTO;
private LocalDateTime fechaCreacion;
private LocalDateTime fechaModificacion;

@PostLoad
private void establecerEstadoPorDefectoParaRegistrosExistentes() {
    if (estado == null) {
        estado = EstadoIncidencia.ABIERTO;
    }
}
}
