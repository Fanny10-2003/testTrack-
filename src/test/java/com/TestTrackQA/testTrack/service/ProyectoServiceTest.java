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
import org.springframework.security.access.AccessDeniedException;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void listarProyectosDelUsuarioIncluyeElRolDeSuMembresia() {
        Proyecto proyecto = new Proyecto();
        proyecto.setIdProyecto(12L);
        proyecto.setNombre("Proyecto compartido");

        MiembroProyecto membresia = new MiembroProyecto();
        membresia.setProyecto(proyecto);
        membresia.setRol(RolProyecto.QA);

        when(miembroProyectoRepository
                .findAllByUsuario_CorreoIgnoreCaseAndProyecto_EliminadoFalse("qa@test.com"))
                .thenReturn(List.of(membresia));
        when(incidenciaService.traerIncidenciasPorProyectos(List.of(12L))).thenReturn(List.of());

        List<ProyectoDTO> proyectos = proyectoService.listarProyectosDelUsuario("qa@test.com");

        assertEquals(1, proyectos.size());
        assertEquals(12L, proyectos.get(0).getIdProyecto());
        assertEquals("QA", proyectos.get(0).getRolUsuario());
    }

    @Test
    void buscarPropioPermiteCualquierRolDeMembresia() {
        Proyecto proyecto = new Proyecto();
        proyecto.setIdProyecto(12L);

        MiembroProyecto membresia = new MiembroProyecto();
        membresia.setProyecto(proyecto);
        membresia.setRol(RolProyecto.DESARROLLADOR);
        when(miembroProyectoRepository
                .findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                        "dev@test.com", 12L))
                .thenReturn(Optional.of(membresia));

        ProyectoDTO resultado = proyectoService.buscarPropio(12L, "dev@test.com");

        assertEquals(12L, resultado.getIdProyecto());
    }

    @Test
    void editarRequiereRolAdministradorEnLaMembresia() {
        MiembroProyecto membresia = new MiembroProyecto();
        membresia.setProyecto(new Proyecto());
        membresia.setRol(RolProyecto.QA);
        when(miembroProyectoRepository
                .findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                        "qa@test.com", 12L))
                .thenReturn(Optional.of(membresia));

        assertThrows(AccessDeniedException.class,
                () -> proyectoService.buscarPropioComoAdministrador(12L, "qa@test.com"));
        assertThrows(AccessDeniedException.class,
                () -> proyectoService.editar(12L, new ProyectoDTO(), "qa@test.com"));
    }

    @Test
    void editarDevuelveNotFoundCuandoNoHayMembresia() {
        when(miembroProyectoRepository
                .findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                        "ausente@test.com", 12L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> proyectoService.editar(12L, new ProyectoDTO(), "ausente@test.com"));
    }

    @Test
    void editarPermiteAdministradorDeMembresiaAunqueNoSeaElCreadorHistorico() {
        Proyecto proyecto = new Proyecto();
        proyecto.setIdProyecto(12L);
        proyecto.setAdministrador(new Usuario());

        MiembroProyecto membresia = new MiembroProyecto();
        membresia.setProyecto(proyecto);
        membresia.setRol(RolProyecto.ADMINISTRADOR);
        when(miembroProyectoRepository
                .findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(
                        "admin@test.com", 12L))
                .thenReturn(Optional.of(membresia));

        ProyectoDTO datos = new ProyectoDTO();
        datos.setNombre("Nombre actualizado");
        proyectoService.editar(12L, datos, "admin@test.com");

        assertEquals("Nombre actualizado", proyecto.getNombre());
    }
}
