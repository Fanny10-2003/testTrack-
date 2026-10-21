package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.Proyecto;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IIncidenciaRepository;
import com.TestTrackQA.testTrack.repository.IProyectoRepository;
import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IncidenciaService implements IIncidenciaService {

    private final IIncidenciaRepository incidenciaRepository;
    private final IProyectoRepository proyectoRepository;
    private final IUsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Incidencia> traerIncidencias() {
        List<Incidencia> incidencias = incidenciaRepository.findAll();
        incidencias.forEach(this::inicializarRelaciones);
        return incidencias;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incidencia> traerIncidenciasPorProyecto(Long proyectoId) {
        return incidenciaRepository.findAll().stream()
                .filter(incidencia -> incidencia.getProyecto().getIdProyecto().equals(proyectoId))
                .peek(this::inicializarRelaciones)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Incidencia> traerIncidenciasPorProyectos(List<Long> proyectoIds) {
        Set<Long> ids = Set.copyOf(proyectoIds);
        return incidenciaRepository.findAll().stream()
                .filter(incidencia -> ids.contains(incidencia.getProyecto().getIdProyecto()))
                .peek(this::inicializarRelaciones)
                .toList();
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

        Incidencia guardada = incidenciaRepository.save(incidencia);
        guardada.setCodigoIncidencia(String.format("BUG-%03d", guardada.getIdIncidencia()));
        return incidenciaRepository.save(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Incidencia buscarIncidencia(Long codIncidencia) {
        Incidencia incidencia = incidenciaRepository.findById(codIncidencia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la incidencia con ID " + codIncidencia));
        inicializarRelaciones(incidencia);
        return incidencia;
    }

    @Override
    @Transactional(readOnly = true)
    public Incidencia buscarIncidenciaEnProyecto(Long codIncidencia, Long proyectoId) {
        Incidencia incidencia = incidenciaRepository.findById(codIncidencia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la incidencia en el proyecto activo"));
        if (!incidencia.getProyecto().getIdProyecto().equals(proyectoId)) {
            throw new EntityNotFoundException("No se encontró la incidencia en el proyecto activo");
        }
        inicializarRelaciones(incidencia);
        return incidencia;
    }

    @Override
    @Transactional
    public Incidencia editarIncidencia(Long codIncidencia, Incidencia cambios,
                                       Long proyectoId, Long usuarioId) {
        Incidencia existente = buscarIncidencia(codIncidencia);
        asignarProyectoYUsuario(cambios, proyectoId, usuarioId);
        validarIncidencia(cambios);
        copiarDatosEditables(cambios, existente);
        existente.setProyecto(cambios.getProyecto());
        existente.setUsuario(cambios.getUsuario());
        existente.setFechaModificacion(LocalDateTime.now());
        return incidenciaRepository.save(existente);
    }

    @Override
    @Transactional
    public Incidencia editarIncidenciaEnProyecto(Long codIncidencia, Incidencia cambios, Long proyectoId) {
        Incidencia existente = buscarIncidenciaEnProyecto(codIncidencia, proyectoId);
        cambios.setProyecto(existente.getProyecto());
        cambios.setUsuario(existente.getUsuario());
        validarIncidencia(cambios);
        copiarDatosEditables(cambios, existente);
        existente.setFechaModificacion(LocalDateTime.now());
        return incidenciaRepository.save(existente);
    }

    @Override
    @Transactional
    public Incidencia cambiarEstado(Long codIncidencia, EstadoIncidencia nuevoEstado) {
        Incidencia incidencia = buscarIncidencia(codIncidencia);
        validarTransicion(incidencia, nuevoEstado);
        incidencia.setEstado(nuevoEstado);
        incidencia.setFechaModificacion(LocalDateTime.now());
        return incidenciaRepository.save(incidencia);
    }

    @Override
    @Transactional
    public Incidencia cambiarEstadoEnProyecto(Long codIncidencia, Long proyectoId,
                                               EstadoIncidencia nuevoEstado) {
        Incidencia incidencia = buscarIncidenciaEnProyecto(codIncidencia, proyectoId);
        validarTransicion(incidencia, nuevoEstado);
        incidencia.setEstado(nuevoEstado);
        incidencia.setFechaModificacion(LocalDateTime.now());
        return incidenciaRepository.save(incidencia);
    }

    private void copiarDatosEditables(Incidencia origen, Incidencia destino) {
        destino.setTitulo(origen.getTitulo());
        destino.setModulo(origen.getModulo());
        destino.setEntornoVersion(origen.getEntornoVersion());
        destino.setDescripcion(origen.getDescripcion());
        destino.setPasos(origen.getPasos());
        destino.setResultadoEsperado(origen.getResultadoEsperado());
        destino.setResultadoActual(origen.getResultadoActual());
        destino.setSeveridad(origen.getSeveridad());
        destino.setComentario(origen.getComentario());
        destino.setEvidencia(origen.getEvidencia());
    }

    private void inicializarRelaciones(Incidencia incidencia) {
        Hibernate.initialize(incidencia.getProyecto());
        Hibernate.initialize(incidencia.getUsuario());
    }

    private void validarTransicion(Incidencia incidencia, EstadoIncidencia nuevoEstado) {
        EstadoIncidencia actual = incidencia.getEstado();
        boolean permitida =
                (actual == EstadoIncidencia.ABIERTO && nuevoEstado == EstadoIncidencia.CERRADO)
                || (actual == EstadoIncidencia.CERRADO && nuevoEstado == EstadoIncidencia.ABIERTO);
        if (actual != nuevoEstado && !permitida) {
            throw new IllegalStateException("No se permite cambiar el estado de " + actual + " a " + nuevoEstado);
        }
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
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new IllegalArgumentException("El proyecto seleccionado no existe"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario seleccionado no existe"));
        incidencia.setProyecto(proyecto);
        incidencia.setUsuario(usuario);
    }
}
