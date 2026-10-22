package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.DTO.HistorialIncidenciaDTO;
import com.TestTrackQA.testTrack.DTO.ComentarioIncidenciaDTO;
import com.TestTrackQA.testTrack.model.ComentarioIncidencia;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.HistorialIncidencia;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.MiembroProyecto;
import com.TestTrackQA.testTrack.model.Proyecto;
import com.TestTrackQA.testTrack.model.RolProyecto;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IIncidenciaRepository;
import com.TestTrackQA.testTrack.repository.IComentarioIncidenciaRepository;
import com.TestTrackQA.testTrack.repository.IHistorialIncidenciaRepository;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class IncidenciaService implements IIncidenciaService {

    private static final Pattern CODIGO_INCIDENCIA_PATTERN = Pattern.compile("^BUG-(\\d+)$");

    private final IIncidenciaRepository incidenciaRepository;
    private final IComentarioIncidenciaRepository comentarioIncidenciaRepository;
    private final IHistorialIncidenciaRepository historialIncidenciaRepository;
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
        Incidencia creada = incidenciaRepository.save(guardada);
        registrarCambio(creada, usuario, "incidencia", null, creada.getTitulo(), LocalDateTime.now());
        return creada;
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
        Usuario actor = buscarUsuario(correoUsuario);
        LocalDateTime fechaCambio = LocalDateTime.now();
        registrarCambiosEditables(existente, cambios, actor, fechaCambio);
        copiarDatosEditables(cambios, existente);
        existente.setFechaModificacion(fechaCambio);
        return incidenciaRepository.save(existente);
    }

    @Override
    @Transactional
    public Incidencia cambiarEstadoEnProyecto(Long codIncidencia, Long proyectoId,
                                               EstadoIncidencia nuevoEstado, String correoUsuario) {
        Incidencia incidencia = buscarIncidenciaEnProyectoParaActualizar(codIncidencia, proyectoId);
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        RolProyecto rolActor = obtenerRolEnProyecto(correoUsuario, proyectoId);
        validarTransicionEstado(incidencia, nuevoEstado, rolActor);
        if ((incidencia.getEstado() == EstadoIncidencia.EN_PROGRESO
                && nuevoEstado == EstadoIncidencia.RESUELTO)
                || (incidencia.getEstado() == EstadoIncidencia.RESUELTO
                && nuevoEstado == EstadoIncidencia.EN_PROGRESO)) {
            validarDesarrolladorAsignado(incidencia, correoUsuario);
        }
        if (incidencia.getEstado() == nuevoEstado) {
            return incidencia;
        }
        EstadoIncidencia estadoAnterior = incidencia.getEstado();
        LocalDateTime fechaCambio = LocalDateTime.now();
        incidencia.setEstado(nuevoEstado);
        incidencia.setFechaModificacion(fechaCambio);
        registrarCambioSiDistinto(incidencia, buscarUsuario(correoUsuario), "estado",
                estadoAnterior, nuevoEstado, fechaCambio);
        return incidenciaRepository.save(incidencia);
    }

    @Override
    @Transactional
    public Incidencia actualizarAsignacionEnProyecto(
            Long codIncidencia, Long proyectoId, Long usuarioId, String correoUsuario) {
        Incidencia incidencia = buscarIncidenciaEnProyectoParaActualizar(codIncidencia, proyectoId);
        RolProyecto rolActor = obtenerRolEnProyecto(correoUsuario, proyectoId);
        if (rolActor == RolProyecto.QA) {
            throw new AccessDeniedException("Tu rol no permite cambiar la asignación");
        }
        Usuario actor = buscarUsuario(correoUsuario);
        Usuario asignadoActual = incidencia.getDesarrolladorAsignado();

        if (usuarioId == null) {
            if (asignadoActual == null) {
                return incidencia;
            }
            if (incidencia.getEstado() == EstadoIncidencia.RESUELTO) {
                throw new IllegalStateException("No se puede liberar una incidencia resuelta");
            }
            if (rolActor != RolProyecto.ADMINISTRADOR
                    && !asignadoActual.getIdUsuario().equals(actor.getIdUsuario())) {
                throw new AccessDeniedException("Solo puedes liberar tu propia asignación");
            }
            incidencia.setDesarrolladorAsignado(null);
            registrarCambio(incidencia, actor, "asignado_a",
                    asignadoActual.getNombre(), null, LocalDateTime.now());
        } else {
            if (incidencia.getEstado() != EstadoIncidencia.ABIERTO
                    && incidencia.getEstado() != EstadoIncidencia.EN_PROGRESO) {
                throw new IllegalStateException("Solo se puede asignar una incidencia abierta o en progreso");
            }
            MiembroProyecto miembroAsignado = miembroProyectoRepository
                    .findByUsuario_IdUsuarioAndProyecto_IdProyectoAndProyecto_EliminadoFalse(usuarioId, proyectoId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "El usuario seleccionado no pertenece al proyecto"));
            RolProyecto rolAsignado = miembroAsignado.getRol();
            if (rolAsignado != RolProyecto.DESARROLLADOR && rolAsignado != RolProyecto.ADMINISTRADOR) {
                throw new IllegalArgumentException("Solo se puede asignar la incidencia a un desarrollador");
            }
            if (rolActor != RolProyecto.ADMINISTRADOR && !actor.getIdUsuario().equals(usuarioId)) {
                throw new AccessDeniedException("Solo puedes tomarte una incidencia a ti mismo");
            }
            Usuario nuevoAsignado = miembroAsignado.getUsuario();
            if (asignadoActual != null && asignadoActual.getIdUsuario().equals(usuarioId)) {
                return incidencia;
            }
            if (rolActor != RolProyecto.ADMINISTRADOR && asignadoActual != null) {
                throw new IllegalStateException("La incidencia ya está asignada a otro desarrollador");
            }
            LocalDateTime fechaCambio = LocalDateTime.now();
            incidencia.setDesarrolladorAsignado(nuevoAsignado);
            registrarCambio(incidencia, actor, "asignado_a",
                    asignadoActual == null ? null : asignadoActual.getNombre(),
                    nuevoAsignado.getNombre(), fechaCambio);
            if (incidencia.getEstado() == EstadoIncidencia.ABIERTO) {
                incidencia.setEstado(EstadoIncidencia.EN_PROGRESO);
                registrarCambio(incidencia, actor, "estado",
                        EstadoIncidencia.ABIERTO.name(), EstadoIncidencia.EN_PROGRESO.name(), fechaCambio);
            }
        }

        incidencia.setFechaModificacion(LocalDateTime.now());
        return incidenciaRepository.save(incidencia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroProyecto> traerMiembrosDelProyecto(Long proyectoId) {
        return miembroProyectoRepository.findAllByProyecto_IdProyectoAndProyecto_EliminadoFalse(proyectoId);
    }

    @Override
    @Transactional(readOnly = true)
    public RolProyecto obtenerRolUsuarioEnProyecto(String correoUsuario, Long proyectoId) {
        return obtenerRolEnProyecto(correoUsuario, proyectoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialIncidenciaDTO> traerHistorial(Long codIncidencia, String correoUsuario) {
        Incidencia incidencia = incidenciaRepository.findById(codIncidencia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la incidencia con ID " + codIncidencia));
        obtenerRolEnProyecto(correoUsuario, incidencia.getProyecto().getIdProyecto());
        return historialIncidenciaRepository
                .findAllByIncidencia_IdIncidenciaOrderByFechaDescIdDesc(codIncidencia)
                .stream()
                .map(HistorialIncidenciaDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public ComentarioIncidenciaDTO agregarComentario(Long codIncidencia, String contenido, String correoUsuario) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }
        Incidencia incidencia = incidenciaRepository.findById(codIncidencia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la incidencia con ID " + codIncidencia));
        obtenerRolEnProyecto(correoUsuario, incidencia.getProyecto().getIdProyecto());
        Usuario autor = buscarUsuario(correoUsuario);

        ComentarioIncidencia comentario = new ComentarioIncidencia(
                incidencia, autor, contenido.trim(), LocalDateTime.now());
        return ComentarioIncidenciaDTO.fromEntity(comentarioIncidenciaRepository.save(comentario));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioIncidenciaDTO> traerComentarios(Long codIncidencia, String correoUsuario) {
        Incidencia incidencia = incidenciaRepository.findById(codIncidencia)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró la incidencia con ID " + codIncidencia));
        obtenerRolEnProyecto(correoUsuario, incidencia.getProyecto().getIdProyecto());
        return comentarioIncidenciaRepository
                .findAllByIncidencia_IdIncidenciaOrderByFechaCreacionAscIdAsc(codIncidencia)
                .stream()
                .map(ComentarioIncidenciaDTO::fromEntity)
                .toList();
    }

    private void registrarCambiosEditables(
            Incidencia anterior, Incidencia nuevo, Usuario actor, LocalDateTime fechaCambio) {
        registrarCambioSiDistinto(anterior, actor, "título", anterior.getTitulo(), nuevo.getTitulo(), fechaCambio);
        registrarCambioSiDistinto(anterior, actor, "módulo", anterior.getModulo(), nuevo.getModulo(), fechaCambio);
        registrarCambioSiDistinto(anterior, actor, "entorno/versión",
                anterior.getEntornoVersion(), nuevo.getEntornoVersion(), fechaCambio);
        registrarCambioSiDistinto(anterior, actor, "descripción",
                anterior.getDescripcion(), nuevo.getDescripcion(), fechaCambio);
        registrarCambioSiDistinto(anterior, actor, "pasos",
                anterior.getPasos(), nuevo.getPasos(), fechaCambio);
        registrarCambioSiDistinto(anterior, actor, "resultado esperado",
                anterior.getResultadoEsperado(), nuevo.getResultadoEsperado(), fechaCambio);
        registrarCambioSiDistinto(anterior, actor, "resultado actual",
                anterior.getResultadoActual(), nuevo.getResultadoActual(), fechaCambio);
        registrarCambioSiDistinto(anterior, actor, "severidad",
                anterior.getSeveridad(), nuevo.getSeveridad(), fechaCambio);
        registrarCambioSiDistinto(anterior, actor, "comentario",
                anterior.getComentario(), nuevo.getComentario(), fechaCambio);
        registrarCambioSiDistinto(anterior, actor, "evidencia",
                anterior.getEvidencia(), nuevo.getEvidencia(), fechaCambio);
    }

    private void registrarCambioSiDistinto(
            Incidencia incidencia, Usuario actor, String campo, Object anterior, Object nuevo, LocalDateTime fecha) {
        if (!Objects.equals(anterior, nuevo)) {
            registrarCambio(incidencia, actor, campo, valorComoTexto(anterior), valorComoTexto(nuevo), fecha);
        }
    }

    private String valorComoTexto(Object valor) {
        return valor == null ? null : valor.toString();
    }

    private void registrarCambio(
            Incidencia incidencia, Usuario actor, String campo, String anterior, String nuevo, LocalDateTime fecha) {
        HistorialIncidencia historial = new HistorialIncidencia();
        historial.setIncidencia(incidencia);
        historial.setUsuario(actor);
        historial.setCampoModificado(campo);
        historial.setValorAnterior(anterior);
        historial.setValorNuevo(nuevo);
        historial.setFecha(fecha);
        historialIncidenciaRepository.save(historial);
    }

    private Usuario buscarUsuario(String correoUsuario) {
        return usuarioRepository.findByCorreoIgnoreCase(correoUsuario)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario"));
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

    private void validarTransicionEstado(
            Incidencia incidencia, EstadoIncidencia nuevoEstado, RolProyecto rolActor) {
        EstadoIncidencia actual = incidencia.getEstado();
        if (actual == nuevoEstado) {
            return;
        }
        boolean transicionQa = (actual == EstadoIncidencia.ABIERTO && nuevoEstado == EstadoIncidencia.CERRADO)
                || (actual == EstadoIncidencia.CERRADO && nuevoEstado == EstadoIncidencia.ABIERTO);
        boolean transicionDesarrollador =
                (actual == EstadoIncidencia.EN_PROGRESO && nuevoEstado == EstadoIncidencia.RESUELTO)
                        || (actual == EstadoIncidencia.RESUELTO && nuevoEstado == EstadoIncidencia.EN_PROGRESO);

        boolean permitida = switch (rolActor) {
            case QA -> transicionQa;
            case DESARROLLADOR -> transicionDesarrollador;
            case ADMINISTRADOR -> transicionQa || transicionDesarrollador;
        };

        if (!permitida) {
            String accionPermitida = switch (rolActor) {
                case QA -> "QA solo puede cambiar entre ABIERTO y CERRADO";
                case DESARROLLADOR -> "DESARROLLADOR solo puede cambiar entre EN_PROGRESO y RESUELTO";
                case ADMINISTRADOR ->
                        "ADMINISTRADOR solo puede cambiar entre ABIERTO/CERRADO o EN_PROGRESO/RESUELTO";
            };
            throw new AccessDeniedException(accionPermitida);
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

    private void validarDesarrolladorAsignado(Incidencia incidencia, String correoUsuario) {
        if (incidencia.getDesarrolladorAsignado() == null
                || !incidencia.getDesarrolladorAsignado().getCorreo().equalsIgnoreCase(correoUsuario)) {
            throw new AccessDeniedException("Solo el desarrollador asignado puede realizar esta acción");
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

}
