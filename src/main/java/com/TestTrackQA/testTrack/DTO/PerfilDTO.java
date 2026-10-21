package com.TestTrackQA.testTrack.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PerfilDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Escribe un correo válido")
    @Size(max = 254, message = "El correo no puede superar los 254 caracteres")
    private String correo;

    private String fotoPerfil;
    private String contrasenaActual;
    private String nuevaContrasena;
    private String confirmarContrasena;
}
