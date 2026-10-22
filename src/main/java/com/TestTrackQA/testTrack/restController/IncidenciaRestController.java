package com.TestTrackQA.testTrack.restController;

import com.TestTrackQA.testTrack.DTO.IncidenciaDTO;
import com.TestTrackQA.testTrack.DTO.ComentarioIncidenciaDTO;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.service.IIncidenciaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping ("/api/incidencias")
@RequiredArgsConstructor
public class IncidenciaRestController {

    private final IIncidenciaService incidenciaService;

    @GetMapping
    public List<IncidenciaDTO> traerIncidencias() {
        return incidenciaService.traerIncidencias().stream()
                .map(IncidenciaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{codIncidencia}")
    public ResponseEntity<IncidenciaDTO> buscarIncidencia(@PathVariable Long codIncidencia) {
        try {
            return ResponseEntity.ok(IncidenciaDTO.fromEntity(incidenciaService.buscarIncidencia(codIncidencia)));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{codIncidencia}/historial")
    public ResponseEntity<?> traerHistorial(
            @PathVariable Long codIncidencia, Authentication authentication) {
        try {
            return ResponseEntity.ok(incidenciaService.traerHistorial(codIncidencia, authentication.getName()));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

    @GetMapping("/{codIncidencia}/comentarios")
    public ResponseEntity<?> traerComentarios(
            @PathVariable Long codIncidencia, Authentication authentication) {
        try {
            return ResponseEntity.ok(
                    incidenciaService.traerComentarios(codIncidencia, authentication.getName()));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{codIncidencia}/comentarios")
    public ResponseEntity<?> agregarComentario(
            @PathVariable Long codIncidencia,
            @RequestBody ComentarioRequest solicitud,
            Authentication authentication) {
        try {
            ComentarioIncidenciaDTO comentario = incidenciaService.agregarComentario(
                    codIncidencia, solicitud == null ? null : solicitud.contenido(), authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crearIncidencia(@Valid @RequestBody IncidenciaDTO incidencia,
                                             Authentication authentication) {
        try {
            Incidencia incidenciaCreada = incidenciaService.crearIncidencia(incidencia.toEntity(),
                    incidencia.getProyectoId(), authentication.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(IncidenciaDTO.fromEntity(incidenciaCreada));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{codIncidencia}")
    public ResponseEntity<?> editarIncidencia(
            @PathVariable Long codIncidencia,
            @Valid @RequestBody IncidenciaDTO incidencia,
            Authentication authentication) {
        try {
            Incidencia actualizada = incidenciaService.editarIncidenciaEnProyecto(codIncidencia, incidencia.toEntity(),
                    incidencia.getProyectoId(), authentication.getName());
            return ResponseEntity.ok(IncidenciaDTO.fromEntity(actualizada));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PatchMapping("/{codIncidencia}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long codIncidencia,
                                          @RequestParam EstadoIncidencia estado,
                                          @RequestParam Long proyectoId,
                                          Authentication authentication) {
        try {
            return ResponseEntity.ok(IncidenciaDTO.fromEntity(incidenciaService.cambiarEstadoEnProyecto(
                    codIncidencia, proyectoId, estado, authentication.getName())));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{codIncidencia}/asignar")
    public ResponseEntity<?> asignarIncidencia(
            @PathVariable Long codIncidencia,
            @RequestParam(required = false) Long usuarioId,
            Authentication authentication,
            HttpSession session) {
        Object proyectoActivo = session.getAttribute("proyectoActivoId");
        if (!(proyectoActivo instanceof Long proyectoId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", "No hay un proyecto activo"));
        }
        try {
            Incidencia actualizada = incidenciaService.actualizarAsignacionEnProyecto(
                    codIncidencia, proyectoId, usuarioId, authentication.getName());
            return ResponseEntity.ok(new AsignacionResponse(
                    actualizada.getIdIncidencia(),
                    actualizada.getDesarrolladorAsignado() == null
                            ? null : actualizada.getDesarrolladorAsignado().getIdUsuario(),
                    actualizada.getEstado().name()));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("error", ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    private record AsignacionResponse(Long incidenciaId, Long usuarioId, String estado) { }
    private record ComentarioRequest(String contenido) { }
}
