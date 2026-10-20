package com.TestTrackQA.testTrack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "proyectos")
public class Proyecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProyecto;
    private String nombre;
    private String descripcion;
    private String urlBase;
    private LocalDateTime fecha_creacion;
    private LocalDateTime fecha_modificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_administrador", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario administrador;

    @Column(nullable = false)
    private boolean eliminado = false;

    @OneToMany(mappedBy = "proyecto", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Incidencia> incidencias = new ArrayList<>();
}
