package com.TestTrackQA.testTrack.DTO;

import com.TestTrackQA.testTrack.model.Usuario;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UsuarioDTO {

    private Long idUsuario;
    private String nombre;
    private String correo;
    private String rol;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setRol(rol);
        usuario.setFechaCreacion(fechaCreacion);
        usuario.setFechaModificacion(fechaModificacion);
        return usuario;
    }

    public static UsuarioDTO fromEntity(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setCorreo(usuario.getCorreo());
        dto.setRol(usuario.getRol());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setFechaModificacion(usuario.getFechaModificacion());
        return dto;
    }
}
