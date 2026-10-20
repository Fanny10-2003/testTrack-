package com.TestTrackQA.testTrack.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroUsuarioDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Escribe un correo válido")
    @Size(max = 254, message = "El correo no puede superar los 254 caracteres")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String contrasena;

    @NotBlank(message = "Confirma la contraseña")
    private String confirmarContrasena;
}
