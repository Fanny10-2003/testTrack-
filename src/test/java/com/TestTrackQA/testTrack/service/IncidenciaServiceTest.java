package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.Proyecto;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IIncidenciaRepository;
import com.TestTrackQA.testTrack.repository.IProyectoRepository;
import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @InjectMocks
    private IncidenciaService incidenciaService;

    @Test
    void crearIncidenciaIniciaNumeracionEnCadaProyecto() {
        Proyecto proyectoNuevo = proyecto(2L);
        Proyecto otroProyecto = proyecto(1L);
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);

        when(proyectoRepository.findById(2L)).thenReturn(Optional.of(proyectoNuevo));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(incidenciaRepository.findAll()).thenReturn(List.of(incidencia(otroProyecto, "BUG-001")));
        when(incidenciaRepository.save(any(Incidencia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia creada = incidenciaService.crearIncidencia(nuevaIncidencia(), 2L, 10L);

        assertEquals("BUG-001", creada.getCodigoIncidencia());
    }

    @Test
    void crearIncidenciaContinuaDesdeElMayorCodigoDelMismoProyecto() {
        Proyecto proyectoActual = proyecto(2L);
        Proyecto otroProyecto = proyecto(1L);
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(10L);

        when(proyectoRepository.findById(2L)).thenReturn(Optional.of(proyectoActual));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(incidenciaRepository.findAll()).thenReturn(List.of(
                incidencia(proyectoActual, "BUG-001"),
                incidencia(proyectoActual, "BUG-004"),
                incidencia(otroProyecto, "BUG-019")));
        when(incidenciaRepository.save(any(Incidencia.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incidencia creada = incidenciaService.crearIncidencia(nuevaIncidencia(), 2L, 10L);

        assertEquals("BUG-005", creada.getCodigoIncidencia());
    }

    private Proyecto proyecto(Long id) {
        Proyecto proyecto = new Proyecto();
        proyecto.setIdProyecto(id);
        return proyecto;
    }

    private Incidencia incidencia(Proyecto proyecto, String codigo) {
        Incidencia incidencia = new Incidencia();
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
