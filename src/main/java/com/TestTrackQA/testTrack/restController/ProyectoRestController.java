package com.TestTrackQA.testTrack.restController;

import com.TestTrackQA.testTrack.DTO.ProyectoDTO;
import com.TestTrackQA.testTrack.service.IProyectoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectoRestController {

    private final IProyectoService proyectoService;

    @GetMapping
    public List<ProyectoDTO> listar(Authentication authentication) {
        return proyectoService.listarProyectosDelUsuario(authentication.getName());
    }

    @GetMapping("/{idProyecto}")
    public ResponseEntity<ProyectoDTO> buscar(@PathVariable Long idProyecto, Authentication authentication) {
        try {
            return ResponseEntity.ok(proyectoService.buscarPropio(idProyecto, authentication.getName()));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ProyectoDTO> crear(
            @Valid @RequestBody ProyectoDTO proyecto,
            Authentication authentication) {
        String correoAdministrador = authentication.getName();
        Long idProyecto = proyectoService.crear(proyecto, correoAdministrador);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{idProyecto}")
                .buildAndExpand(idProyecto)
                .toUri();
        return ResponseEntity.created(location)
                .body(proyectoService.buscarPropio(idProyecto, correoAdministrador));
    }

    @PutMapping("/{idProyecto}")
    public ResponseEntity<ProyectoDTO> editar(
            @PathVariable Long idProyecto,
            @Valid @RequestBody ProyectoDTO proyecto,
            Authentication authentication) {
        try {
            String correoAdministrador = authentication.getName();
            proyectoService.editar(idProyecto, proyecto, correoAdministrador);
            return ResponseEntity.ok(proyectoService.buscarPropio(idProyecto, correoAdministrador));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{idProyecto}")
    public ResponseEntity<Void> eliminar(@PathVariable Long idProyecto, Authentication authentication) {
        try {
            proyectoService.eliminar(idProyecto, authentication.getName());
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
