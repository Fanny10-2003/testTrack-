package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.ComentarioIncidencia;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Locale;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidenciaServiceTest {

    private static final AtomicLong NEXT_USER_ID = new AtomicLong();
    private static final ConcurrentHashMap<String, Long> USER_IDS = new ConcurrentHashMap<>();

    @Mock
    private IIncidenciaRepository incidenciaRepository;

    @Mock
    private IHistorialIncidenciaRepository historialIncidenciaRepository;

    @Mock
    private IComentarioIncidenciaRepository comentarioIncidenciaRepository;

    @Mock
    private IProyectoRepository proyectoRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IMiembroProyectoRepository miembroProyectoRepository;

    @InjectMocks
    private IncidenciaService incidenciaService;

    @Test
    void usuarioAsignaUnIdPorCorreoNormalizado() {
        Usuario dev = usuario("dev@test.com");
        Usuario qa = usuario("qa@test.com");
        Usuario devMayusculas = usuario("DEV@test.com");

        assertNotEquals(dev.getIdUsuario(), qa.getIdUsuario());
        assertEquals(dev.getIdUsuario(), devMayusculas.getIdUsuario());
    }

    @Test
    void crearIncidenciaIniciaNumeracionPorProyectoYGuardaReporteroAutenticado() {
        Proyecto proyecto = proyecto(2L);
        Usuario qa = usuario("qa@test.com");
        when(proyectoRepository.findById(2L)).thenReturn(Optional.of(proyecto));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "qa@test.com", 2L)).thenReturn(Optional.of(membresia(qa, proyecto, RolProyecto.QA)));
        when(usuarioRepository.findByCorreoIgnoreCase("qa@test.com")).thenReturn(Optional.of(qa));
        when(incidenciaRepository.findAll()).thenReturn(List.of(incidencia(proyecto(1L), "BUG-001")));
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia creada = incidenciaService.crearIncidencia(nuevaIncidencia(), 2L, "qa@test.com");

        assertEquals("BUG-001", creada.getCodigoIncidencia());
        assertEquals(qa, creada.getUsuario());
        verify(historialIncidenciaRepository).save(any(HistorialIncidencia.class));
    }

    @Test
    void crearIncidenciaDeniegaDesarrollador() {
        Proyecto proyecto = proyecto(2L);
        Usuario dev = usuario("dev@test.com");
        when(proyectoRepository.findById(2L)).thenReturn(Optional.of(proyecto));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "dev@test.com", 2L)).thenReturn(Optional.of(membresia(dev, proyecto, RolProyecto.DESARROLLADOR)));

        assertThrows(AccessDeniedException.class,
                () -> incidenciaService.crearIncidencia(nuevaIncidencia(), 2L, "dev@test.com"));
    }

    @Test
    void agregarComentarioDeniegaUsuarioSinMembresia() {
        Proyecto proyecto = proyecto(2L);
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        when(incidenciaRepository.findById(3L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository
                .findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse("externo@test.com", 2L))
                .thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> incidenciaService.agregarComentario(3L, "Mensaje", "externo@test.com"));
    }

    @Test
    void agregarComentarioRechazaContenidoVacioOEnBlanco() {
        assertThrows(IllegalArgumentException.class,
                () -> incidenciaService.agregarComentario(3L, null, "qa@test.com"));
        assertThrows(IllegalArgumentException.class,
                () -> incidenciaService.agregarComentario(3L, "   \n ", "qa@test.com"));
    }

    @Test
    void miembroPuedePublicarComentarioYSeGuardaSinEspaciosExternos() {
        Proyecto proyecto = proyecto(2L);
        Usuario qa = usuario("qa@test.com");
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        when(incidenciaRepository.findById(3L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository
                .findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse("qa@test.com", 2L))
                .thenReturn(Optional.of(membresia(qa, proyecto, RolProyecto.QA)));
        when(usuarioRepository.findByCorreoIgnoreCase("qa@test.com")).thenReturn(Optional.of(qa));
        when(comentarioIncidenciaRepository.save(any(ComentarioIncidencia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var comentario = incidenciaService.agregarComentario(3L, "  Ya lo reproduje.  ", "qa@test.com");

        assertEquals("Ya lo reproduje.", comentario.getContenido());
        assertEquals("qa@test.com", comentario.getUsuarioNombre());
        verify(comentarioIncidenciaRepository).save(any(ComentarioIncidencia.class));
    }

    @Test
    void actualizarAsignacionPermiteTomarYDejarSinAsignar() {
        Proyecto proyecto = proyecto(2L);
        Usuario dev = usuario("dev@test.com");
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        incidencia.setEstado(EstadoIncidencia.ABIERTO);
        MiembroProyecto membresia = membresia(dev, proyecto, RolProyecto.DESARROLLADOR);
        when(incidenciaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "dev@test.com", 2L)).thenReturn(Optional.of(membresia));
        when(miembroProyectoRepository.findByUsuario_IdUsuarioAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                dev.getIdUsuario(), 2L)).thenReturn(Optional.of(membresia));
        when(usuarioRepository.findByCorreoIgnoreCase("dev@test.com")).thenReturn(Optional.of(dev));
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia asignada = incidenciaService.actualizarAsignacionEnProyecto(
                3L, 2L, dev.getIdUsuario(), "dev@test.com");
        assertEquals(dev, asignada.getDesarrolladorAsignado());
        assertEquals(EstadoIncidencia.EN_PROGRESO, asignada.getEstado());

        Incidencia liberada = incidenciaService.actualizarAsignacionEnProyecto(
                3L, 2L, null, "dev@test.com");
        assertNull(liberada.getDesarrolladorAsignado());
        assertEquals(EstadoIncidencia.EN_PROGRESO, liberada.getEstado());
        verify(historialIncidenciaRepository, org.mockito.Mockito.times(3))
                .save(any(HistorialIncidencia.class));
    }

    @Test
    void actualizarAsignacionNoPermiteAsignarIncidenciaCerradaNiLiberarLaResuelta() {
        Proyecto proyecto = proyecto(2L);
        Usuario dev = usuario("dev@test.com");
        MiembroProyecto membresia = membresia(dev, proyecto, RolProyecto.DESARROLLADOR);
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "dev@test.com", 2L)).thenReturn(Optional.of(membresia));
        when(usuarioRepository.findByCorreoIgnoreCase("dev@test.com")).thenReturn(Optional.of(dev));

        Incidencia cerrada = incidencia(proyecto, "BUG-001");
        cerrada.setEstado(EstadoIncidencia.CERRADO);
        when(incidenciaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(cerrada));
        assertThrows(IllegalStateException.class, () -> incidenciaService.actualizarAsignacionEnProyecto(
                3L, 2L, dev.getIdUsuario(), "dev@test.com"));

        Incidencia resuelta = incidencia(proyecto, "BUG-002");
        resuelta.setEstado(EstadoIncidencia.RESUELTO);
        resuelta.setDesarrolladorAsignado(dev);
        when(incidenciaRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(resuelta));
        assertThrows(IllegalStateException.class, () -> incidenciaService.actualizarAsignacionEnProyecto(
                4L, 2L, null, "dev@test.com"));
    }

    @Test
    void actualizarAsignacionSoloPermiteAlDesarrolladorLiberarSuPropiaIncidencia() {
        Proyecto proyecto = proyecto(2L);
        Usuario dev = usuario("dev@test.com");
        Usuario otro = usuario("otro@test.com");
        otro.setIdUsuario(20L);
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        incidencia.setEstado(EstadoIncidencia.EN_PROGRESO);
        incidencia.setDesarrolladorAsignado(otro);
        when(incidenciaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "dev@test.com", 2L)).thenReturn(Optional.of(membresia(dev, proyecto, RolProyecto.DESARROLLADOR)));
        when(usuarioRepository.findByCorreoIgnoreCase("dev@test.com")).thenReturn(Optional.of(dev));

        assertThrows(AccessDeniedException.class, () -> incidenciaService.actualizarAsignacionEnProyecto(
                3L, 2L, null, "dev@test.com"));
    }

    @Test
    void qaPuedeCerrarYReabrirPeroNoResolver() {
        Proyecto proyecto = proyecto(2L);
        Usuario qa = usuario("qa@test.com");
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        incidencia.setEstado(EstadoIncidencia.ABIERTO);
        when(incidenciaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "qa@test.com", 2L)).thenReturn(Optional.of(membresia(qa, proyecto, RolProyecto.QA)));
        when(usuarioRepository.findByCorreoIgnoreCase("qa@test.com")).thenReturn(Optional.of(qa));
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia cerrada = incidenciaService.cambiarEstadoEnProyecto(
                1L, 2L, EstadoIncidencia.CERRADO, "qa@test.com");

        assertEquals(EstadoIncidencia.CERRADO, cerrada.getEstado());
        verify(historialIncidenciaRepository).save(any(HistorialIncidencia.class));
        Incidencia reabierta = incidenciaService.cambiarEstadoEnProyecto(
                1L, 2L, EstadoIncidencia.ABIERTO, "qa@test.com");
        assertEquals(EstadoIncidencia.ABIERTO, reabierta.getEstado());
        assertThrows(AccessDeniedException.class, () -> incidenciaService.cambiarEstadoEnProyecto(
                1L, 2L, EstadoIncidencia.EN_PROGRESO, "qa@test.com"));
        assertThrows(AccessDeniedException.class, () -> incidenciaService.cambiarEstadoEnProyecto(
                1L, 2L, EstadoIncidencia.RESUELTO, "qa@test.com"));
    }

    @Test
    void desarrolladorAsignadoPuedeResolverYReabrirAEnProgreso() {
        Proyecto proyecto = proyecto(2L);
        Usuario dev = usuario("dev@test.com");
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        incidencia.setEstado(EstadoIncidencia.EN_PROGRESO);
        incidencia.setDesarrolladorAsignado(dev);
        when(incidenciaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "dev@test.com", 2L)).thenReturn(Optional.of(membresia(dev, proyecto, RolProyecto.DESARROLLADOR)));
        when(usuarioRepository.findByCorreoIgnoreCase("dev@test.com")).thenReturn(Optional.of(dev));
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia resuelta = incidenciaService.cambiarEstadoEnProyecto(
                3L, 2L, EstadoIncidencia.RESUELTO, "dev@test.com");
        assertEquals(EstadoIncidencia.RESUELTO, resuelta.getEstado());

        Incidencia enProgreso = incidenciaService.cambiarEstadoEnProyecto(
                3L, 2L, EstadoIncidencia.EN_PROGRESO, "dev@test.com");
        assertEquals(EstadoIncidencia.EN_PROGRESO, enProgreso.getEstado());
        assertThrows(AccessDeniedException.class, () -> incidenciaService.cambiarEstadoEnProyecto(
                3L, 2L, EstadoIncidencia.CERRADO, "dev@test.com"));
    }

    @Test
    void administradorPuedeCerrarYReabrirPeroNoHacerTransicionesCruzadas() {
        Proyecto proyecto = proyecto(2L);
        Usuario admin = usuario("admin@test.com");
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        incidencia.setEstado(EstadoIncidencia.ABIERTO);
        when(incidenciaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "admin@test.com", 2L))
                .thenReturn(Optional.of(membresia(admin, proyecto, RolProyecto.ADMINISTRADOR)));
        when(usuarioRepository.findByCorreoIgnoreCase("admin@test.com")).thenReturn(Optional.of(admin));
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia cerrada = incidenciaService.cambiarEstadoEnProyecto(
                3L, 2L, EstadoIncidencia.CERRADO, "admin@test.com");

        assertEquals(EstadoIncidencia.CERRADO, cerrada.getEstado());
        assertThrows(AccessDeniedException.class, () -> incidenciaService.cambiarEstadoEnProyecto(
                3L, 2L, EstadoIncidencia.EN_PROGRESO, "admin@test.com"));
    }

    @Test
    void desarrolladorNoPuedeCambiarEstadoSiLaIncidenciaNoEstaAsignadaASiMismo() {
        Proyecto proyecto = proyecto(2L);
        Usuario dev = usuario("dev@test.com");
        Incidencia sinAsignar = incidencia(proyecto, "BUG-001");
        sinAsignar.setEstado(EstadoIncidencia.EN_PROGRESO);

        Incidencia asignadaAOtro = incidencia(proyecto, "BUG-002");
        asignadaAOtro.setEstado(EstadoIncidencia.EN_PROGRESO);
        asignadaAOtro.setDesarrolladorAsignado(usuario("otro@test.com"));

        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "dev@test.com", 2L)).thenReturn(Optional.of(membresia(dev, proyecto, RolProyecto.DESARROLLADOR)));
        when(incidenciaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sinAsignar));
        when(incidenciaRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(asignadaAOtro));

        assertThrows(AccessDeniedException.class, () -> incidenciaService.cambiarEstadoEnProyecto(
                1L, 2L, EstadoIncidencia.RESUELTO, "dev@test.com"));
        assertThrows(AccessDeniedException.class, () -> incidenciaService.cambiarEstadoEnProyecto(
                2L, 2L, EstadoIncidencia.RESUELTO, "dev@test.com"));
    }

    private Proyecto proyecto(Long id) {
        Proyecto proyecto = new Proyecto();
        proyecto.setIdProyecto(id);
        return proyecto;
    }

    private Usuario usuario(String correo) {
        Usuario usuario = new Usuario();
        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);
        usuario.setIdUsuario(USER_IDS.computeIfAbsent(
                correoNormalizado, ignored -> NEXT_USER_ID.incrementAndGet()));
        usuario.setCorreo(correo);
        usuario.setNombre(correo);
        return usuario;
    }

    private MiembroProyecto membresia(Usuario usuario, Proyecto proyecto, RolProyecto rol) {
        MiembroProyecto miembro = new MiembroProyecto();
        miembro.setUsuario(usuario);
        miembro.setProyecto(proyecto);
        miembro.setRol(rol);
        return miembro;
    }

    private Incidencia incidencia(Proyecto proyecto, String codigo) {
        Incidencia incidencia = new Incidencia();
        incidencia.setIdIncidencia(3L);
        incidencia.setProyecto(proyecto);
        incidencia.setCodigoIncidencia(codigo);
        return incidencia;
    }

    private Incidencia nuevaIncidencia() {
        Incidencia incidencia = new Incidencia();
        incidencia.setTitulo("Incidencia de prueba");
        incidencia.setModulo("Módulo");
        incidencia.setDescripcion("Descripción");
        incidencia.setSeveridad("MEDIA");
        return incidencia;
    }
}
