package com.TestTrackQA.testTrack.restController;

import com.TestTrackQA.testTrack.DTO.IncidenciaDTO;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.service.IIncidenciaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping ("/api/incidencias")
public class IncidenciaRestController {

    private final IIncidenciaService prodServ;

    public IncidenciaRestController(IIncidenciaService prodServ) {
        this.prodServ = prodServ;
    }


    @GetMapping
    public List<Incidencia> traerIncidencias() {
        return prodServ.traerIncidencias();
    }

    @GetMapping("/{codIncidencia}")
    public ResponseEntity<Incidencia> buscarIncidencia(@PathVariable Long codIncidencia) {
        try {
            return ResponseEntity.ok(prodServ.buscarIncidencia(codIncidencia));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Incidencia> crearIncidencia(@Valid @RequestBody IncidenciaDTO incidencia) {
        Incidencia incidenciaCreada = prodServ.crearIncidencia(incidencia.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(incidenciaCreada);
    }

    @PutMapping("/{codIncidencia}")
    public ResponseEntity<Incidencia> editarIncidencia(
            @PathVariable Long codIncidencia,
            @Valid @RequestBody IncidenciaDTO incidencia) {
        try {
            return ResponseEntity.ok(prodServ.editarIncidencia(codIncidencia, incidencia.toEntity()));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{codIncidencia}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long codIncidencia,
                                          @RequestParam EstadoIncidencia estado) {
        try {
            return ResponseEntity.ok(prodServ.cambiarEstado(codIncidencia, estado));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
