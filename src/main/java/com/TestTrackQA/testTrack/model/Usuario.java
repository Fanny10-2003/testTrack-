package com.TestTrackQA.testTrack.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 254)
    private String correo;

    @NotBlank
    @Column(name = "password_hash", nullable = false, length = 100)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String contrasena;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String rol;
    @Column(name = "foto_perfil", length = 100)
    private String fotoPerfil;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Incidencia> incidenciasReportadas = new ArrayList<>();

    @OneToMany(mappedBy = "administrador", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Proyecto> proyectosAdministrados = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MiembroProyecto> membresiasProyecto = new ArrayList<>();
}
