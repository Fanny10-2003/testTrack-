package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.Proyecto;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IIncidenciaRepository;
import com.TestTrackQA.testTrack.repository.IProyectoRepository;
import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class IncidenciaService implements  IIncidenciaService {

    private final IIncidenciaRepository prodRepo;
    private final IProyectoRepository proyectoRepo;
    private final IUsuarioRepository usuarioRepo;


    @Override
    public List<Incidencia> traerIncidencias() {
        return prodRepo.findAllWithProyectoAndUsuario();
    }

    @Override
    @Transactional
    public Incidencia crearIncidencia(Incidencia incidencia, Long proyectoId, Long usuarioId) {
        asignarProyectoYUsuario(incidencia, proyectoId, usuarioId);
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
        return prodRepo.findWithProyectoAndUsuarioById(codIncidencia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la incidencia con ID " + codIncidencia));
    }

    @Override
    @Transactional
    public Incidencia editarIncidencia(Long codIncidencia, Incidencia incidencia,
                                       Long proyectoId, Long usuarioId) {
        Incidencia incidenciaExistente = buscarIncidencia(codIncidencia);
        asignarProyectoYUsuario(incidencia, proyectoId, usuarioId);
        validarIncidencia(incidencia);
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
        incidenciaExistente.setProyecto(incidencia.getProyecto());
        incidenciaExistente.setUsuario(incidencia.getUsuario());
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
        if (incidencia.getProyecto() == null || incidencia.getUsuario() == null) {
            throw new IllegalArgumentException("El proyecto y el usuario que reporta son obligatorios");
        }
    }

    private void asignarProyectoYUsuario(Incidencia incidencia, Long proyectoId, Long usuarioId) {
        Proyecto proyecto = proyectoRepo.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("El proyecto seleccionado no existe"));
        Usuario usuario = usuarioRepo.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario seleccionado no existe"));
        incidencia.setProyecto(proyecto);
        incidencia.setUsuario(usuario);
    }
}
