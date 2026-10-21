package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.DTO.ProyectoDTO;
import com.TestTrackQA.testTrack.model.Proyecto;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.MiembroProyecto;
import com.TestTrackQA.testTrack.model.RolProyecto;
import com.TestTrackQA.testTrack.repository.IMiembroProyectoRepository;
import com.TestTrackQA.testTrack.repository.IProyectoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoService implements IProyectoService {

    private final IProyectoRepository proyectoRepository;
    private final IMiembroProyectoRepository miembroProyectoRepository;
    private final IUsuarioService usuarioService;
    private final IIncidenciaService incidenciaService;

    @Override
    @Transactional(readOnly = true)
    public List<ProyectoDTO> listarProyectosDelUsuario(String correo) {
        List<MiembroProyecto> membresias = miembroProyectoRepository
                .findAllByUsuario_CorreoIgnoreCaseAndProyecto_EliminadoFalse(correo);
        if (membresias.isEmpty()) {
            return List.of();
        }

        Map<Long, ProyectoDTO> proyectosPorId = membresias.stream()
                .map(membresia -> {
                    ProyectoDTO dto = ProyectoDTO.fromEntity(membresia.getProyecto());
                    dto.setRolUsuario(nombreRol(membresia.getRol()));
                    return dto;
                })
                .collect(Collectors.toMap(ProyectoDTO::getIdProyecto, Function.identity()));
        List<Long> idsProyecto = proyectosPorId.keySet().stream().toList();
        List<Incidencia> incidencias = incidenciaService.traerIncidenciasPorProyectos(idsProyecto);

        incidencias.forEach(incidencia -> {
            ProyectoDTO resumen = proyectosPorId.get(incidencia.getProyecto().getIdProyecto());
            if (resumen == null) {
                return;
            }
            resumen.setTotalIncidencias(resumen.getTotalIncidencias() + 1);
            if (incidencia.getEstado() == EstadoIncidencia.ABIERTO) {
                resumen.setIncidenciasAbiertas(resumen.getIncidenciasAbiertas() + 1);
                if ("CRITICA".equals(incidencia.getSeveridad())) {
                    resumen.setCriticasAbiertas(resumen.getCriticasAbiertas() + 1);
                }
            } else if (incidencia.getEstado() == EstadoIncidencia.CERRADO) {
                resumen.setIncidenciasCerradas(resumen.getIncidenciasCerradas() + 1);
            }
        });

        return membresias.stream()
                .map(membresia -> proyectosPorId.get(membresia.getProyecto().getIdProyecto()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String nombreAdministrador(String correo) {
        return usuarioService.nombrePorCorreo(correo);
    }

    @Override
    @Transactional
    public Long crear(ProyectoDTO datos, String correoAdministrador) {
        var administrador = buscarAdministrador(correoAdministrador);
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(datos.getNombre().trim());
        proyecto.setDescripcion(datos.getDescripcion());
        proyecto.setUrlBase(datos.getUrlBase());
        proyecto.setAdministrador(administrador);
        proyecto.setFecha_creacion(LocalDateTime.now());
        proyecto.setEliminado(false);
        Proyecto proyectoGuardado = proyectoRepository.save(proyecto);

        MiembroProyecto miembroAdministrador = new MiembroProyecto();
        miembroAdministrador.setUsuario(administrador);
        miembroAdministrador.setProyecto(proyectoGuardado);
        miembroAdministrador.setRol(RolProyecto.ADMINISTRADOR);
        miembroProyectoRepository.save(miembroAdministrador);

        return proyectoGuardado.getIdProyecto();
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoDTO buscarPropio(Long idProyecto, String correoAdministrador) {
        return ProyectoDTO.fromEntity(buscarProyectoMiembro(idProyecto, correoAdministrador));
    }

    @Override
    @Transactional(readOnly = true)
    public ProyectoDTO buscarPropioComoAdministrador(Long idProyecto, String correoAdministrador) {
        return ProyectoDTO.fromEntity(buscarProyectoAdministrador(idProyecto, correoAdministrador));
    }

    @Override
    @Transactional
    public void editar(Long idProyecto, ProyectoDTO datos, String correoAdministrador) {
        Proyecto proyecto = buscarProyectoAdministrador(idProyecto, correoAdministrador);
        proyecto.setNombre(datos.getNombre().trim());
        proyecto.setDescripcion(datos.getDescripcion());
        proyecto.setUrlBase(datos.getUrlBase());
        proyecto.setFecha_modificacion(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void eliminar(Long idProyecto, String correoAdministrador) {
        Proyecto proyecto = buscarProyectoAdministrador(idProyecto, correoAdministrador);
        proyecto.setEliminado(true);
        proyecto.setFecha_modificacion(LocalDateTime.now());
    }

    private Usuario buscarAdministrador(String correo) {
        return usuarioService.buscarPorCorreo(correo);
    }

    private Proyecto buscarProyectoMiembro(Long idProyecto, String correo) {
        return miembroProyectoRepository
                .findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(correo, idProyecto)
                .map(MiembroProyecto::getProyecto)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el proyecto"));
    }

    private Proyecto buscarProyectoAdministrador(Long idProyecto, String correo) {
        MiembroProyecto membresia = miembroProyectoRepository
                .findByUsuario_CorreoIgnoreCaseAndProyecto_IdProyectoAndProyecto_EliminadoFalse(correo, idProyecto)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el proyecto"));
        if (membresia.getRol() != RolProyecto.ADMINISTRADOR) {
            throw new AccessDeniedException("Se requiere el rol ADMINISTRADOR para modificar este proyecto");
        }
        return membresia.getProyecto();
    }

    private String nombreRol(RolProyecto rol) {
        return switch (rol) {
            case ADMINISTRADOR -> "Administrador";
            case QA -> "QA";
            case DESARROLLADOR -> "Desarrollador";
        };
    }
}
