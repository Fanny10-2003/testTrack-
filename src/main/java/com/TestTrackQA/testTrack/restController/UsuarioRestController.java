package com.TestTrackQA.testTrack.restController;

import com.TestTrackQA.testTrack.DTO.UsuarioDTO;
import com.TestTrackQA.testTrack.service.IUsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {

    private final IUsuarioService usuarioService;

    @GetMapping("/actual")
    public ResponseEntity<UsuarioDTO> obtenerUsuarioActual(Authentication authentication) {
        try {
            return ResponseEntity.ok(
                    UsuarioDTO.fromEntity(usuarioService.buscarPorCorreo(authentication.getName())));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
