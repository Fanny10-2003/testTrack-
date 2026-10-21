package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.MiembroProyecto;
import com.TestTrackQA.testTrack.model.Proyecto;
import com.TestTrackQA.testTrack.model.RolProyecto;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IIncidenciaRepository;
import com.TestTrackQA.testTrack.repository.IMiembroProyectoRepository;
import com.TestTrackQA.testTrack.repository.IProyectoRepository;
import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class IncidenciaService implements IIncidenciaService {

    private static final Pattern CODIGO_INCIDENCIA_PATTERN = Pattern.compile("^BUG-(\\d+)$");

    private final IIncidenciaRepository incidenciaRepository;
    private final IProyectoRepository proyectoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IMiembroProyectoRepository miembroProyectoRepository;

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
    public Incidencia crearIncidencia(Incidencia incidencia, Long proyectoId, String correoUsuario) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .filter(proyectoEncontrado -> !proyectoEncontrado.isEliminado())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el proyecto"));
        validarRolEnProyecto(correoUsuario, proyectoId, RolProyecto.QA, RolProyecto.ADMINISTRADOR);
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correoUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario"));
        incidencia.setProyecto(proyecto);
        incidencia.setUsuario(usuario);
        validarIncidencia(incidencia);
        incidencia.setIdIncidencia(null);
        incidencia.setFechaCreacion(LocalDateTime.now());
        incidencia.setFechaModificacion(null);
        incidencia.setEstado(EstadoIncidencia.ABIERTO);
        incidencia.setCodigoIncidencia(null);
        incidencia.setDesarrolladorAsignado(null);

        int siguienteNumero = incidenciaRepository.findAll().stream()
                .filter(registrada -> registrada.getProyecto().getIdProyecto().equals(proyectoId))
                .mapToInt(this::obtenerNumeroCodigo)
                .max()
                .orElse(0) + 1;

        Incidencia guardada = incidenciaRepository.save(incidencia);
        guardada.setCodigoIncidencia(String.format("BUG-%03d", siguienteNumero));
        return incidenciaRepository.save(guardada);
    }

    private int obtenerNumeroCodigo(Incidencia incidencia) {
        String codigo = incidencia.getCodigoIncidencia();
        if (codigo == null) {
            return 0;
        }
        Matcher matcher = CODIGO_INCIDENCIA_PATTERN.matcher(codigo);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : 0;
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
    public Incidencia editarIncidenciaEnProyecto(Long codIncidencia, Incidencia cambios,
                                                  Long proyectoId, String correoUsuario) {
        Incidencia existente = buscarIncidenciaEnProyectoParaActualizar(codIncidencia, proyectoId);
        validarRolEnProyecto(correoUsuario, proyectoId, RolProyecto.QA, RolProyecto.ADMINISTRADOR);
        cambios.setProyecto(existente.getProyecto());
        cambios.setUsuario(existente.getUsuario());
        validarIncidencia(cambios);
        copiarDatosEditables(cambios, existente);
        existente.setFechaModificacion(LocalDateTime.now());
        return incidenciaRepository.save(existente);
    }

    @Override
    @Transactional
    public Incidencia cambiarEstadoEnProyecto(Long codIncidencia, Long proyectoId,
                                               EstadoIncidencia nuevoEstado, String correoUsuario) {
        Incidencia incidencia = buscarIncidenciaEnProyectoParaActualizar(codIncidencia, proyectoId);
        RolProyecto rol = obtenerRolEnProyecto(correoUsuario, proyectoId);
        validarTransicion(incidencia, nuevoEstado, rol);
        incidencia.setEstado(nuevoEstado);
        incidencia.setFechaModificacion(LocalDateTime.now());
        return incidenciaRepository.save(incidencia);
    }

    @Override
    @Transactional
    public Incidencia tomarIncidencia(Long codIncidencia, String correoUsuario) {
        Incidencia incidencia = buscarIncidenciaParaActualizar(codIncidencia);
        validarRolEnProyecto(correoUsuario, incidencia.getProyecto().getIdProyecto(),
                RolProyecto.DESARROLLADOR, RolProyecto.ADMINISTRADOR);
        if (incidencia.getEstado() != EstadoIncidencia.ABIERTO
                && incidencia.getEstado() != EstadoIncidencia.EN_PROGRESO) {
            throw new IllegalStateException("Solo se pueden tomar incidencias abiertas o en progreso");
        }
        if (incidencia.getDesarrolladorAsignado() != null) {
            throw new IllegalStateException("La incidencia ya está asignada a un desarrollador");
        }

        Usuario desarrollador = usuarioRepository.findByCorreoIgnoreCase(correoUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario"));
        incidencia.setDesarrolladorAsignado(desarrollador);
        incidencia.setEstado(EstadoIncidencia.EN_PROGRESO);
        incidencia.setFechaModificacion(LocalDateTime.now());
        return incidenciaRepository.save(incidencia);
    }

    @Override
    @Transactional
    public Incidencia liberarIncidencia(Long codIncidencia, String correoUsuario) {
        Incidencia incidencia = buscarIncidenciaParaActualizar(codIncidencia);
        validarRolEnProyecto(correoUsuario, incidencia.getProyecto().getIdProyecto(),
                RolProyecto.DESARROLLADOR, RolProyecto.ADMINISTRADOR);
        if (incidencia.getDesarrolladorAsignado() == null
                || !incidencia.getDesarrolladorAsignado().getCorreo().equalsIgnoreCase(correoUsuario)) {
            throw new AccessDeniedException("Solo el desarrollador asignado puede liberar la incidencia");
        }
        if (incidencia.getEstado() == EstadoIncidencia.RESUELTO) {
            throw new IllegalStateException("No se puede liberar una incidencia resuelta");
        }
        incidencia.setDesarrolladorAsignado(null);
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
        Hibernate.initialize(incidencia.getDesarrolladorAsignado());
    }

    private void validarTransicion(Incidencia incidencia, EstadoIncidencia nuevoEstado, RolProyecto rol) {
        EstadoIncidencia actual = incidencia.getEstado();
        boolean permiteQa = rol == RolProyecto.QA || rol == RolProyecto.ADMINISTRADOR;
        boolean permiteDesarrollador = rol == RolProyecto.DESARROLLADOR || rol == RolProyecto.ADMINISTRADOR;
        boolean estadoQa = nuevoEstado == EstadoIncidencia.ABIERTO || nuevoEstado == EstadoIncidencia.CERRADO;
        boolean estadoDesarrollador =
                nuevoEstado == EstadoIncidencia.EN_PROGRESO || nuevoEstado == EstadoIncidencia.RESUELTO;
        if ((estadoQa && !permiteQa) || (estadoDesarrollador && !permiteDesarrollador)) {
            throw new AccessDeniedException("Tu rol no permite realizar este cambio de estado");
        }

        boolean transicionQa = (actual == EstadoIncidencia.ABIERTO && nuevoEstado == EstadoIncidencia.CERRADO)
                || (actual == EstadoIncidencia.CERRADO && nuevoEstado == EstadoIncidencia.ABIERTO);
        boolean transicionDesarrollador =
                (actual == EstadoIncidencia.EN_PROGRESO && nuevoEstado == EstadoIncidencia.RESUELTO)
                || (actual == EstadoIncidencia.RESUELTO && nuevoEstado == EstadoIncidencia.EN_PROGRESO);

        if (actual == nuevoEstado) {
            return;
        }
        if (!transicionQa && !transicionDesarrollador) {
            throw new IllegalStateException("No se permite cambiar el estado de " + actual + " a " + nuevoEstado);
        }
    }

    private Incidencia buscarIncidenciaParaActualizar(Long codIncidencia) {
        Incidencia incidencia = incidenciaRepository.findByIdForUpdate(codIncidencia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la incidencia con ID " + codIncidencia));
        inicializarRelaciones(incidencia);
        return incidencia;
    }

    private Incidencia buscarIncidenciaEnProyectoParaActualizar(Long codIncidencia, Long proyectoId) {
        Incidencia incidencia = buscarIncidenciaParaActualizar(codIncidencia);
        if (!incidencia.getProyecto().getIdProyecto().equals(proyectoId)) {
            throw new EntityNotFoundException("No se encontró la incidencia en el proyecto activo");
        }
        return incidencia;
    }

    private void validarRolEnProyecto(String correo, Long proyectoId, RolProyecto... rolesPermitidos) {
        RolProyecto rol = obtenerRolEnProyecto(correo, proyectoId);
        for (RolProyecto rolPermitido : rolesPermitidos) {
            if (rol == rolPermitido) {
                return;
            }
        }
        throw new AccessDeniedException("Tu rol no permite realizar esta acción");
    }

    private RolProyecto obtenerRolEnProyecto(String correo, Long proyectoId) {
        Optional<MiembroProyecto> membresia = miembroProyectoRepository
                .findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(correo, proyectoId);
        return membresia.map(MiembroProyecto::getRol)
                .orElseThrow(() -> new AccessDeniedException("No tienes membresía en este proyecto"));
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

}
