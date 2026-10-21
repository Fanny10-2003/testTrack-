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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidenciaServiceTest {

    @Mock
    private IIncidenciaRepository incidenciaRepository;

    @Mock
    private IProyectoRepository proyectoRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IMiembroProyectoRepository miembroProyectoRepository;

    @InjectMocks
    private IncidenciaService incidenciaService;

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
    void tomarIncidenciaAsignaAlDesarrolladorYLaPasaAEnProgreso() {
        Proyecto proyecto = proyecto(2L);
        Usuario dev = usuario("dev@test.com");
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        incidencia.setEstado(EstadoIncidencia.ABIERTO);
        when(incidenciaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "dev@test.com", 2L)).thenReturn(Optional.of(membresia(dev, proyecto, RolProyecto.DESARROLLADOR)));
        when(usuarioRepository.findByCorreoIgnoreCase("dev@test.com")).thenReturn(Optional.of(dev));
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia tomada = incidenciaService.tomarIncidencia(3L, "dev@test.com");

        assertEquals(dev, tomada.getDesarrolladorAsignado());
        assertEquals(EstadoIncidencia.EN_PROGRESO, tomada.getEstado());
    }

    @Test
    void noPermiteTomarIncidenciaCerradaNiYaAsignada() {
        Proyecto proyecto = proyecto(2L);
        Usuario dev = usuario("dev@test.com");
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "dev@test.com", 2L)).thenReturn(Optional.of(membresia(dev, proyecto, RolProyecto.DESARROLLADOR)));

        Incidencia cerrada = incidencia(proyecto, "BUG-001");
        cerrada.setEstado(EstadoIncidencia.CERRADO);
        when(incidenciaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(cerrada));
        assertThrows(IllegalStateException.class, () -> incidenciaService.tomarIncidencia(1L, "dev@test.com"));

        Incidencia asignada = incidencia(proyecto, "BUG-002");
        asignada.setEstado(EstadoIncidencia.EN_PROGRESO);
        asignada.setDesarrolladorAsignado(usuario("otro@test.com"));
        when(incidenciaRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(asignada));
        assertThrows(IllegalStateException.class, () -> incidenciaService.tomarIncidencia(2L, "dev@test.com"));
    }

    @Test
    void liberarIncidenciaQuitaAsignacionSinResolver() {
        Proyecto proyecto = proyecto(2L);
        Usuario dev = usuario("dev@test.com");
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        incidencia.setEstado(EstadoIncidencia.EN_PROGRESO);
        incidencia.setDesarrolladorAsignado(dev);
        when(incidenciaRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "dev@test.com", 2L)).thenReturn(Optional.of(membresia(dev, proyecto, RolProyecto.DESARROLLADOR)));
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia liberada = incidenciaService.liberarIncidencia(3L, "dev@test.com");

        assertNull(liberada.getDesarrolladorAsignado());
        assertEquals(EstadoIncidencia.EN_PROGRESO, liberada.getEstado());
    }

    @Test
    void estadoSoloCambiaEnTransicionesPermitidasParaRol() {
        Proyecto proyecto = proyecto(2L);
        Usuario qa = usuario("qa@test.com");
        Incidencia incidencia = incidencia(proyecto, "BUG-001");
        incidencia.setEstado(EstadoIncidencia.ABIERTO);
        when(incidenciaRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(incidencia));
        when(miembroProyectoRepository.findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                "qa@test.com", 2L)).thenReturn(Optional.of(membresia(qa, proyecto, RolProyecto.QA)));
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia cerrada = incidenciaService.cambiarEstadoEnProyecto(
                1L, 2L, EstadoIncidencia.CERRADO, "qa@test.com");

        assertEquals(EstadoIncidencia.CERRADO, cerrada.getEstado());
        Incidencia reabierta = incidenciaService.cambiarEstadoEnProyecto(
                1L, 2L, EstadoIncidencia.ABIERTO, "qa@test.com");
        assertEquals(EstadoIncidencia.ABIERTO, reabierta.getEstado());
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
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia resuelta = incidenciaService.cambiarEstadoEnProyecto(
                3L, 2L, EstadoIncidencia.RESUELTO, "dev@test.com");
        assertEquals(EstadoIncidencia.RESUELTO, resuelta.getEstado());

        Incidencia enProgreso = incidenciaService.cambiarEstadoEnProyecto(
                3L, 2L, EstadoIncidencia.EN_PROGRESO, "dev@test.com");
        assertEquals(EstadoIncidencia.EN_PROGRESO, enProgreso.getEstado());
    }

    private Proyecto proyecto(Long id) {
        Proyecto proyecto = new Proyecto();
        proyecto.setIdProyecto(id);
        return proyecto;
    }

    private Usuario usuario(String correo) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);
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
