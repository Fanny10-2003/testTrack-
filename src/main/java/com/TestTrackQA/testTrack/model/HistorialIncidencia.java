package com.TestTrackQA.testTrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "historial_incidencias")
public class HistorialIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incidencia_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Incidencia incidencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @Column(name = "campo_modificado", nullable = false, length = 80)
    private String campoModificado;

    @Column(name = "valor_anterior", length = 2000)
    private String valorAnterior;

    @Column(name = "valor_nuevo", length = 2000)
    private String valorNuevo;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
