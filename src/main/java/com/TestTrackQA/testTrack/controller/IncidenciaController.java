package com.TestTrackQA.testTrack.controller;

import com.TestTrackQA.testTrack.DTO.IncidenciaDTO;
import com.TestTrackQA.testTrack.model.EstadoIncidencia;
import com.TestTrackQA.testTrack.model.Incidencia;
import com.TestTrackQA.testTrack.service.IIncidenciaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/incidencias")
public class IncidenciaController {

    private  final IIncidenciaService prodSer;

    public IncidenciaController(IIncidenciaService prodSer) {
        this.prodSer = prodSer;
    }

    @GetMapping
    public String traerIncidencia(Model model) {
        model.addAttribute("incidencias", prodSer.traerIncidencias());
        return "incidencia/listaIncidencia";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("incidencia", new IncidenciaDTO());
        model.addAttribute("titulo", "Registrar incidencia");
        return "incidencia/formulario";
    }

    @PostMapping("/crear")
    public String crearIncidencia(@Valid @ModelAttribute("incidencia") IncidenciaDTO incidencia,
                                  BindingResult bindingResult,
                                  Model model) {
        model.addAttribute("titulo", "Registrar incidencia");
        if (bindingResult.hasErrors()) {
            return "incidencia/formulario";
        }

        prodSer.crearIncidencia(incidencia.toEntity());
        return "redirect:/incidencias";
    }

    @PostMapping("/editar/{codIncidencia}")
    public String editarIncidencia(@PathVariable Long codIncidencia,
                                   @Valid @ModelAttribute("incidencia") IncidenciaDTO incidencia,
                                   BindingResult bindingResult,
                                   Model model) {
        model.addAttribute("titulo", "Editar incidencia");
        if (bindingResult.hasErrors()) {
            incidencia.setIdIncidencia(codIncidencia);
            return "incidencia/formulario";
        }

        try {
            prodSer.editarIncidencia(codIncidencia, incidencia.toEntity());
        } catch (EntityNotFoundException ex) {
            return "redirect:/incidencias";
        }
        return "redirect:/incidencias";
    }

    @GetMapping("/editar/{codIncidencia}")
    public String mostrarFormularioEditar(@PathVariable Long codIncidencia, Model model) {
        try {
            Incidencia incidencia = prodSer.buscarIncidencia(codIncidencia);
            model.addAttribute("incidencia", IncidenciaDTO.fromEntity(incidencia));
        } catch (EntityNotFoundException ex) {
            return "redirect:/incidencias";
        }
        model.addAttribute("titulo", "Editar incidencia");
        return "incidencia/formulario";
    }

    @PostMapping("/{codIncidencia}/estado")
    public String cambiarEstado(@PathVariable Long codIncidencia,
                                @RequestParam EstadoIncidencia estado,
                                RedirectAttributes redirectAttributes) {
        try {
            prodSer.cambiarEstado(codIncidencia, estado);
        } catch (EntityNotFoundException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/incidencias";
    }
}
