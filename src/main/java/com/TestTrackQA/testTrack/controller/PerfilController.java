package com.TestTrackQA.testTrack.controller;

import com.TestTrackQA.testTrack.DTO.PerfilDTO;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.repository.IUsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class PerfilController {

    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @GetMapping("/perfil")
    public String mostrarPerfil(Authentication authentication, Model model) {
        Usuario usuario = buscarUsuarioAutenticado(authentication);
        PerfilDTO perfil = new PerfilDTO();
        perfil.setNombre(usuario.getNombre());
        perfil.setCorreo(usuario.getCorreo());
        prepararVista(model, perfil, usuario.getNombre());
        return "perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(
            @Valid @ModelAttribute("perfil") PerfilDTO perfil,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = buscarUsuarioAutenticado(authentication);
        String nombreNormalizado = perfil.getNombre() == null ? "" : perfil.getNombre().trim();
        String correoNormalizado = perfil.getCorreo() == null
                ? ""
                : perfil.getCorreo().trim().toLowerCase(Locale.ROOT);

        if (!correoNormalizado.isBlank()
                && !correoNormalizado.equalsIgnoreCase(usuario.getCorreo())
                && usuarioRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
            bindingResult.rejectValue("correo", "correo.duplicado",
                    "Ya existe una cuenta con ese correo");
        }

        boolean quiereCambiarContrasena = !estaVacio(perfil.getContrasenaActual())
                || !estaVacio(perfil.getNuevaContrasena())
                || !estaVacio(perfil.getConfirmarContrasena());
        if (quiereCambiarContrasena) {
            if (estaVacio(perfil.getContrasenaActual())) {
                bindingResult.rejectValue("contrasenaActual", "contrasena.actual.obligatoria",
                        "Escribe tu contraseña actual");
            } else if (!passwordEncoder.matches(perfil.getContrasenaActual(), usuario.getContrasena())) {
                bindingResult.rejectValue("contrasenaActual", "contrasena.actual.incorrecta",
                        "La contraseña actual no es correcta");
            }

            if (estaVacio(perfil.getNuevaContrasena())) {
                bindingResult.rejectValue("nuevaContrasena", "contrasena.nueva.obligatoria",
                        "Escribe una contraseña nueva");
            } else if (perfil.getNuevaContrasena().length() < 8
                    || perfil.getNuevaContrasena().length() > 72) {
                bindingResult.rejectValue("nuevaContrasena", "contrasena.nueva.longitud",
                        "La contraseña debe tener entre 8 y 72 caracteres");
            }

            if (estaVacio(perfil.getConfirmarContrasena())) {
                bindingResult.rejectValue("confirmarContrasena", "contrasena.confirmacion.obligatoria",
                        "Confirma la contraseña nueva");
            } else if (!perfil.getConfirmarContrasena().equals(perfil.getNuevaContrasena())) {
                bindingResult.rejectValue("confirmarContrasena", "contrasena.noCoincide",
                        "Las contraseñas nuevas no coinciden");
            }
        }

        if (bindingResult.hasErrors()) {
            perfil.setNombre(nombreNormalizado);
            perfil.setCorreo(correoNormalizado);
            prepararVista(model, perfil, usuario.getNombre());
            return "perfil";
        }

        usuario.setNombre(nombreNormalizado);
        usuario.setCorreo(correoNormalizado);
        usuario.setFechaModificacion(LocalDateTime.now());
        if (quiereCambiarContrasena) {
            usuario.setContrasena(passwordEncoder.encode(perfil.getNuevaContrasena()));
        }
        usuarioRepository.save(usuario);

        actualizarAutenticacion(authentication, usuario, request, response);
        redirectAttributes.addFlashAttribute("mensaje", "Tu perfil se actualizó correctamente.");
        return "redirect:/perfil";
    }

    private Usuario buscarUsuarioAutenticado(Authentication authentication) {
        return usuarioRepository.findByCorreoIgnoreCase(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el usuario autenticado"));
    }

    private void actualizarAutenticacion(
            Authentication authentication,
            Usuario usuario,
            HttpServletRequest request,
            HttpServletResponse response) {
        UserDetails principalActualizado = User.withUsername(usuario.getCorreo())
                .password(usuario.getContrasena())
                .authorities(authentication.getAuthorities())
                .build();
        Authentication autenticacionActualizada = new UsernamePasswordAuthenticationToken(
                principalActualizado, null, authentication.getAuthorities());
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacionActualizada);
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);
    }

    private void prepararVista(Model model, PerfilDTO perfil, String nombreUsuario) {
        model.addAttribute("perfil", perfil);
        model.addAttribute("usuarioNombre", nombreUsuario);
        model.addAttribute("iniciales", iniciales(nombreUsuario));
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
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
