package com.TestTrackQA.testTrack.service;

import com.TestTrackQA.testTrack.DTO.ProyectoDTO;
import com.TestTrackQA.testTrack.model.Proyecto;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.repository.IIncidenciaRepository;
import com.TestTrackQA.testTrack.repository.IProyectoRepository;
import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final IProyectoRepository proyectoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IIncidenciaRepository incidenciaRepository;

    @Transactional(readOnly = true)
    public List<ProyectoDTO> listarDelAdministrador(String correo) {
        List<Proyecto> proyectos = proyectoRepository.findAllByAdministrador_CorreoIgnoreCaseAndEliminadoFalse(correo);
        if (proyectos.isEmpty()) {
            return List.of();
        }

        Map<Long, ProyectoDTO> proyectosPorId = proyectos.stream()
                .map(ProyectoDTO::fromEntity)
                .peek(dto -> dto.setRolUsuario("Administrador"))
                .collect(Collectors.toMap(ProyectoDTO::getIdProyecto, Function.identity()));
        List<Long> idsProyecto = proyectosPorId.keySet().stream().toList();
        List<Incidencia> incidencias = incidenciaRepository.findAllByProyecto_IdProyectoIn(idsProyecto);

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

        return proyectos.stream()
                .map(proyecto -> proyectosPorId.get(proyecto.getIdProyecto()))
                .toList();
    }

    @Transactional(readOnly = true)
    public String nombreAdministrador(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .map(Usuario::getNombre)
                .orElse(correo);
    }

    @Transactional
    public Long crear(ProyectoDTO datos, String correoAdministrador) {
        Usuario administrador = buscarAdministrador(correoAdministrador);
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(datos.getNombre().trim());
        proyecto.setDescripcion(datos.getDescripcion());
        proyecto.setUrlBase(datos.getUrlBase());
        proyecto.setAdministrador(administrador);
        proyecto.setFecha_creacion(LocalDateTime.now());
        proyecto.setEliminado(false);
        return proyectoRepository.save(proyecto).getIdProyecto();
    }

    @Transactional(readOnly = true)
    public ProyectoDTO buscarPropio(Long idProyecto, String correoAdministrador) {
        return ProyectoDTO.fromEntity(buscarProyectoPropio(idProyecto, correoAdministrador));
    }

    @Transactional
    public void editar(Long idProyecto, ProyectoDTO datos, String correoAdministrador) {
        Proyecto proyecto = buscarProyectoPropio(idProyecto, correoAdministrador);
        proyecto.setNombre(datos.getNombre().trim());
        proyecto.setDescripcion(datos.getDescripcion());
        proyecto.setUrlBase(datos.getUrlBase());
        proyecto.setFecha_modificacion(LocalDateTime.now());
    }

    @Transactional
    public void eliminar(Long idProyecto, String correoAdministrador) {
        Proyecto proyecto = buscarProyectoPropio(idProyecto, correoAdministrador);
        proyecto.setEliminado(true);
        proyecto.setFecha_modificacion(LocalDateTime.now());
    }

    private Usuario buscarAdministrador(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario autenticado"));
    }

    private Proyecto buscarProyectoPropio(Long idProyecto, String correoAdministrador) {
        return proyectoRepository
                .findByIdProyectoAndAdministrador_CorreoIgnoreCaseAndEliminadoFalse(idProyecto, correoAdministrador)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el proyecto"));
    }
}
