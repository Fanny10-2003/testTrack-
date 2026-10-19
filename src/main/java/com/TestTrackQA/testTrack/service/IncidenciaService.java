package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.repository.IIncidenciaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class IncidenciaService implements  IIncidenciaService {

    private final IIncidenciaRepository prodRepo;

    public IncidenciaService(IIncidenciaRepository prodRepo) {
        this.prodRepo = prodRepo;
    }

    @Override
    public List<Incidencia> traerIncidencias() {
        return prodRepo.findAll();
    }

    @Override
    @Transactional
    public Incidencia crearIncidencia(Incidencia incidencia) {
        validarIncidencia(incidencia);
        incidencia.setIdIncidencia(null);
        incidencia.setFechaCreacion(LocalDateTime.now());
        incidencia.setFechaModificacion(null);
        incidencia.setEstado(EstadoIncidencia.ABIERTO);
        incidencia.setCodigoIncidencia(null);

        Incidencia guardada = prodRepo.save(incidencia);
        guardada.setCodigoIncidencia(String.format("BUG-%03d", guardada.getIdIncidencia()));
        return prodRepo.save(guardada);
    }

    @Override
    public Incidencia buscarIncidencia(Long codIncidencia) {
        return prodRepo.findById(codIncidencia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la incidencia con ID " + codIncidencia));
    }

    @Override
    public Incidencia editarIncidencia(Long codIncidencia, Incidencia incidencia) {
        validarIncidencia(incidencia);
        Incidencia incidenciaExistente = buscarIncidencia(codIncidencia);
        incidenciaExistente.setTitulo(incidencia.getTitulo());
        incidenciaExistente.setModulo(incidencia.getModulo());
        incidenciaExistente.setEntornoVersion(incidencia.getEntornoVersion());
        incidenciaExistente.setDescripcion(incidencia.getDescripcion());
        incidenciaExistente.setPasos(incidencia.getPasos());
        incidenciaExistente.setResultadoEsperado(incidencia.getResultadoEsperado());
        incidenciaExistente.setResultadoActual(incidencia.getResultadoActual());
        incidenciaExistente.setSeveridad(incidencia.getSeveridad());
        incidenciaExistente.setComentario(incidencia.getComentario());
        incidenciaExistente.setEvidencia(incidencia.getEvidencia());
        incidenciaExistente.setFechaModificacion(LocalDateTime.now());

        return prodRepo.save(incidenciaExistente);
    }

    @Override
    public Incidencia cambiarEstado(Long codIncidencia, EstadoIncidencia nuevoEstado) {
        Incidencia incidencia = buscarIncidencia(codIncidencia);
        EstadoIncidencia estadoActual = incidencia.getEstado();

        if (estadoActual == nuevoEstado) {
            return incidencia;
        }

        boolean transicionPermitida =
                (estadoActual == EstadoIncidencia.ABIERTO && nuevoEstado == EstadoIncidencia.CERRADO)
                || (estadoActual == EstadoIncidencia.CERRADO && nuevoEstado == EstadoIncidencia.ABIERTO);

        if (!transicionPermitida) {
            throw new IllegalStateException(
                    "No se permite cambiar el estado de " + estadoActual + " a " + nuevoEstado);
        }

        incidencia.setEstado(nuevoEstado);
        incidencia.setFechaModificacion(LocalDateTime.now());
        return prodRepo.save(incidencia);
    }

    private void validarIncidencia(Incidencia incidencia) {
        if (incidencia.getTitulo() == null || incidencia.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        if (incidencia.getDescripcion() == null || incidencia.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        if (incidencia.getModulo() == null || incidencia.getModulo().isBlank()) {
            throw new IllegalArgumentException("El módulo es obligatorio");
        }
        if (incidencia.getSeveridad() == null
                || !incidencia.getSeveridad().matches("BAJA|MEDIA|ALTA|CRITICA")) {
            throw new IllegalArgumentException("La severidad debe ser BAJA, MEDIA, ALTA o CRITICA");
        }
    }
}
