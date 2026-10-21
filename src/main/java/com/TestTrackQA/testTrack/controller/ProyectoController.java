package com.TestTrackQA.testTrack.controller;

import com.TestTrackQA.testTrack.DTO.ProyectoDTO;
import com.TestTrackQA.testTrack.service.IProyectoService;
import jakarta.persistence.EntityNotFoundException;
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
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/proyectos")
@RequiredArgsConstructor
public class ProyectoController {

    private final IProyectoService proyectoService;

    @GetMapping
    public String listar(Authentication authentication, HttpSession session, Model model) {
        var proyectos = proyectoService.listarDelAdministrador(authentication.getName());
        String nombreUsuario = proyectoService.nombreAdministrador(authentication.getName());
        model.addAttribute("proyectos", proyectos);
        model.addAttribute("proyectoActivoId", session.getAttribute("proyectoActivoId"));
        model.addAttribute("usuarioNombre", nombreUsuario);
        model.addAttribute("iniciales", iniciales(nombreUsuario));
        model.addAttribute("totalIncidencias", proyectos.stream().mapToLong(ProyectoDTO::getTotalIncidencias).sum());
        model.addAttribute("criticasAbiertas", proyectos.stream().mapToLong(ProyectoDTO::getCriticasAbiertas).sum());
        model.addAttribute("incidenciasAbiertas", proyectos.stream().mapToLong(ProyectoDTO::getIncidenciasAbiertas).sum());
        model.addAttribute("incidenciasCerradas", proyectos.stream().mapToLong(ProyectoDTO::getIncidenciasCerradas).sum());
        return "proyecto/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("proyecto", new ProyectoDTO());
        model.addAttribute("titulo", "Nuevo proyecto");
        return "proyecto/formulario";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("proyecto") ProyectoDTO proyecto,
                        BindingResult bindingResult,
                        Authentication authentication,
                        HttpSession session,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", "Nuevo proyecto");
            return "proyecto/formulario";
        }
        Long nuevoProyectoId = proyectoService.crear(proyecto, authentication.getName());
        session.setAttribute("proyectoActivoId", nuevoProyectoId);
        return "redirect:/proyectos";
    }

    @PostMapping("/{idProyecto}/seleccionar")
    public String seleccionar(@PathVariable Long idProyecto,
                              Authentication authentication,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        try {
            proyectoService.buscarPropio(idProyecto, authentication.getName());
            session.setAttribute("proyectoActivoId", idProyecto);
            return "redirect:/incidencias";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el proyecto solicitado.");
            return "redirect:/proyectos";
        }
    }

    @GetMapping("/{idProyecto}/editar")
    public String editar(@PathVariable Long idProyecto,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("proyecto",
                    proyectoService.buscarPropio(idProyecto, authentication.getName()));
            model.addAttribute("titulo", "Editar proyecto");
            return "proyecto/formulario";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el proyecto solicitado.");
            return "redirect:/proyectos";
        }
    }

    @PostMapping("/{idProyecto}")
    public String actualizar(@PathVariable Long idProyecto,
                             @Valid @ModelAttribute("proyecto") ProyectoDTO proyecto,
                             BindingResult bindingResult,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            proyecto.setIdProyecto(idProyecto);
            model.addAttribute("titulo", "Editar proyecto");
            return "proyecto/formulario";
        }
        try {
            proyectoService.editar(idProyecto, proyecto, authentication.getName());
            return "redirect:/proyectos";
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el proyecto solicitado.");
            return "redirect:/proyectos";
        }
    }

    @PostMapping("/{idProyecto}/eliminar")
    public String eliminar(@PathVariable Long idProyecto,
                           Authentication authentication,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        try {
            proyectoService.eliminar(idProyecto, authentication.getName());
            if (idProyecto.equals(session.getAttribute("proyectoActivoId"))) {
                session.removeAttribute("proyectoActivoId");
            }
            redirectAttributes.addFlashAttribute("mensaje", "Proyecto eliminado.");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el proyecto solicitado.");
        }
        return "redirect:/proyectos";
    }

    private String iniciales(String nombre) {
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) {
            return String.valueOf(partes[0].charAt(0)).toUpperCase(Locale.ROOT);
        }
        return (partes[0].charAt(0) + "" + partes[partes.length - 1].charAt(0))
                .toUpperCase(Locale.ROOT);
    }
}
