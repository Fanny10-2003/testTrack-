package com.TestTrackQA.testTrack.restController;

import com.TestTrackQA.testTrack.DTO.IncidenciaDTO;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.service.IIncidenciaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<?> crearIncidencia(@Valid @RequestBody IncidenciaDTO incidencia) {
        try {
            Incidencia incidenciaCreada = incidenciaService.crearIncidencia(incidencia.toEntity(),
                    incidencia.getProyectoId(), incidencia.getUsuarioId());
            return ResponseEntity.status(HttpStatus.CREATED).body(IncidenciaDTO.fromEntity(incidenciaCreada));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{codIncidencia}")
    public ResponseEntity<?> editarIncidencia(
            @PathVariable Long codIncidencia,
            @Valid @RequestBody IncidenciaDTO incidencia) {
        try {
            Incidencia actualizada = incidenciaService.editarIncidencia(codIncidencia, incidencia.toEntity(),
                    incidencia.getProyectoId(), incidencia.getUsuarioId());
            return ResponseEntity.ok(IncidenciaDTO.fromEntity(actualizada));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PatchMapping("/{codIncidencia}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long codIncidencia,
                                          @RequestParam EstadoIncidencia estado) {
        try {
            return ResponseEntity.ok(incidenciaService.cambiarEstado(codIncidencia, estado));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
