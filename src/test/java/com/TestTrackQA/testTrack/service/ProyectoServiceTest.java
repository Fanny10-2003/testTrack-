package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.DTO.ProyectoDTO;
import com.TestTrackQA.testTrack.model.MiembroProyecto;
import com.TestTrackQA.testTrack.model.Proyecto;
import com.TestTrackQA.testTrack.model.RolProyecto;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IMiembroProyectoRepository;
import com.TestTrackQA.testTrack.repository.IProyectoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProyectoServiceTest {

    @Mock
    private IProyectoRepository proyectoRepository;

    @Mock
    private IMiembroProyectoRepository miembroProyectoRepository;

    @Mock
    private IUsuarioService usuarioService;

    @Mock
    private IIncidenciaService incidenciaService;

    @InjectMocks
    private ProyectoService proyectoService;

    @Test
    void crearRegistraAlCreadorComoAdministradorDelProyecto() {
        Usuario creador = new Usuario();
        creador.setIdUsuario(7L);
        when(usuarioService.buscarPorCorreo("admin@test.com")).thenReturn(creador);
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> {
            Proyecto proyecto = invocation.getArgument(0);
            proyecto.setIdProyecto(12L);
            return proyecto;
        });

        ProyectoDTO datos = new ProyectoDTO();
        datos.setNombre("Proyecto de prueba");
        Long idProyecto = proyectoService.crear(datos, "admin@test.com");

        ArgumentCaptor<MiembroProyecto> miembroCaptor = ArgumentCaptor.forClass(MiembroProyecto.class);
        verify(miembroProyectoRepository).save(miembroCaptor.capture());

        assertEquals(12L, idProyecto);
        assertEquals(creador, miembroCaptor.getValue().getUsuario());
        assertEquals(12L, miembroCaptor.getValue().getProyecto().getIdProyecto());
        assertEquals(RolProyecto.ADMINISTRADOR, miembroCaptor.getValue().getRol());
    }
}
