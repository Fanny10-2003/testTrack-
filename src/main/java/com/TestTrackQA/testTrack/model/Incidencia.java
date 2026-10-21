package com.TestTrackQA.testTrack.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "incidencias",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_incidencia_proyecto_codigo",
                columnNames = {"id_proyecto", "codigo_incidencia"}))
public class Incidencia {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long idIncidencia;
@Column(length = 24)
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

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "id_proyecto", nullable = false)
@ToString.Exclude
@EqualsAndHashCode.Exclude
private Proyecto proyecto;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "id_usuario", nullable = false)
@ToString.Exclude
@EqualsAndHashCode.Exclude
private Usuario usuario;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_desarrollador_asignado")
@ToString.Exclude
@EqualsAndHashCode.Exclude
private Usuario desarrolladorAsignado;

@PostLoad
private void establecerEstadoPorDefectoParaRegistrosExistentes() {
    if (estado == null) {
        estado = EstadoIncidencia.ABIERTO;
    }
}
}
