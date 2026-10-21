package com.TestTrackQA.testTrack.controller;

import com.TestTrackQA.testTrack.DTO.RegistroUsuarioDTO;
import com.TestTrackQA.testTrack.model.Usuario;
import com.TestTrackQA.testTrack.service.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final IUsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registro", new RegistroUsuarioDTO());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @Valid @ModelAttribute("registro") RegistroUsuarioDTO registro,
            BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("contrasena")
                && !bindingResult.hasFieldErrors("confirmarContrasena")
                && !registro.getContrasena().equals(registro.getConfirmarContrasena())) {
            bindingResult.rejectValue("confirmarContrasena", "contrasena.noCoincide",
                    "Las contraseñas no coinciden");
        }

        String correoNormalizado = registro.getCorreo() == null
                ? ""
                : registro.getCorreo().trim().toLowerCase(Locale.ROOT);
        if (!correoNormalizado.isBlank()
                && usuarioService.existePorCorreo(correoNormalizado)) {
            bindingResult.rejectValue("correo", "correo.duplicado",
                    "Ya existe una cuenta con ese correo");
        }

        if (bindingResult.hasErrors()) {
            return "registro";
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(registro.getNombre().trim());
        usuario.setCorreo(correoNormalizado);
        usuario.setContrasena(passwordEncoder.encode(registro.getContrasena()));
        usuario.setRol("ADMINISTRADOR");
        usuario.setFechaCreacion(LocalDateTime.now());
        usuarioService.guardar(usuario);

        return "redirect:/login?registrado";
    }
}
