package com.TestTrackQA.testTrack.controller;

import com.TestTrackQA.testTrack.DTO.IncidenciaDTO;
import com.TestTrackQA.testTrack.DTO.ProyectoDTO;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.service.IIncidenciaService;
import com.TestTrackQA.testTrack.service.IProyectoService;
import com.TestTrackQA.testTrack.service.IUsuarioService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {

    private final IIncidenciaService incidenciaService;
    private final IProyectoService proyectoService;
    private final IUsuarioService usuarioService;

    @GetMapping
    public String listar(Authentication authentication, HttpSession session, Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            ProyectoDTO proyectoActivo = obtenerProyectoActivo(authentication, session);
            model.addAttribute("proyectoActivo", proyectoActivo);
            model.addAttribute("incidencias",
                    incidenciaService.traerIncidenciasPorProyecto(proyectoActivo.getIdProyecto()));
            return "incidencia/listaIncidencia";
        } catch (EntityNotFoundException ex) {
            session.removeAttribute("proyectoActivoId");
            redirectAttributes.addFlashAttribute("error", "Selecciona un proyecto para ver sus incidencias.");
            return "redirect:/proyectos";
        }
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Authentication authentication, HttpSession session, Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            ProyectoDTO proyectoActivo = obtenerProyectoActivo(authentication, session);
            model.addAttribute("incidencia", new IncidenciaDTO());
            model.addAttribute("proyectoActivo", proyectoActivo);
            model.addAttribute("titulo", "Registrar incidencia");
            return "incidencia/formulario";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "Selecciona un proyecto para registrar incidencias.");
            return "redirect:/proyectos";
        }
    }

    @PostMapping("/crear")
    public String crearIncidencia(@Valid @ModelAttribute("incidencia") IncidenciaDTO incidencia,
                                  BindingResult bindingResult,
                                  Authentication authentication,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        ProyectoDTO proyectoActivo;
        Usuario reportero;
        try {
            proyectoActivo = obtenerProyectoActivo(authentication, session);
            reportero = buscarUsuarioAutenticado(authentication);
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "Selecciona un proyecto válido para continuar.");
            return "redirect:/proyectos";
        }

        model.addAttribute("titulo", "Registrar incidencia");
        model.addAttribute("proyectoActivo", proyectoActivo);
        if (bindingResult.hasErrors()) {
            return "incidencia/formulario";
        }

        incidenciaService.crearIncidencia(incidencia.toEntity(),
                proyectoActivo.getIdProyecto(), reportero.getIdUsuario());
        return "redirect:/incidencias";
    }

    @GetMapping("/editar/{codIncidencia}")
    public String mostrarFormularioEditar(@PathVariable Long codIncidencia,
                                          Authentication authentication,
                                          HttpSession session,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            ProyectoDTO proyectoActivo = obtenerProyectoActivo(authentication, session);
            Incidencia incidencia = incidenciaService.buscarIncidenciaEnProyecto(
                    codIncidencia, proyectoActivo.getIdProyecto());
            model.addAttribute("incidencia", IncidenciaDTO.fromEntity(incidencia));
            model.addAttribute("proyectoActivo", proyectoActivo);
            model.addAttribute("titulo", "Editar incidencia");
            return "incidencia/formulario";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la incidencia en el proyecto activo.");
            return "redirect:/incidencias";
        }
    }

    @PostMapping("/editar/{codIncidencia}")
    public String editarIncidencia(@PathVariable Long codIncidencia,
                                   @Valid @ModelAttribute("incidencia") IncidenciaDTO incidencia,
                                   BindingResult bindingResult,
                                   Authentication authentication,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        ProyectoDTO proyectoActivo;
        try {
            proyectoActivo = obtenerProyectoActivo(authentication, session);
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "Selecciona un proyecto válido para continuar.");
            return "redirect:/proyectos";
        }

        model.addAttribute("titulo", "Editar incidencia");
        model.addAttribute("proyectoActivo", proyectoActivo);
        if (bindingResult.hasErrors()) {
            incidencia.setIdIncidencia(codIncidencia);
            return "incidencia/formulario";
        }

        try {
            incidenciaService.editarIncidenciaEnProyecto(
                    codIncidencia, incidencia.toEntity(), proyectoActivo.getIdProyecto());
            return "redirect:/incidencias";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la incidencia en el proyecto activo.");
            return "redirect:/incidencias";
        }
    }

    @PostMapping("/{codIncidencia}/estado")
    public String cambiarEstado(@PathVariable Long codIncidencia,
                                @RequestParam EstadoIncidencia estado,
                                Authentication authentication,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            ProyectoDTO proyectoActivo = obtenerProyectoActivo(authentication, session);
            incidenciaService.cambiarEstadoEnProyecto(
                    codIncidencia, proyectoActivo.getIdProyecto(), estado);
        } catch (EntityNotFoundException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/incidencias";
    }

    private ProyectoDTO obtenerProyectoActivo(Authentication authentication, HttpSession session) {
        Object proyectoId = session.getAttribute("proyectoActivoId");
        if (!(proyectoId instanceof Long id)) {
            throw new EntityNotFoundException("No hay un proyecto seleccionado");
        }
        return proyectoService.buscarPropio(id, authentication.getName());
    }

    private Usuario buscarUsuarioAutenticado(Authentication authentication) {
        return usuarioService.buscarPorCorreo(authentication.getName());
    }
}
