package com.aquasolution.controller;

import com.aquasolution.model.TokenRecuperacion;
import com.aquasolution.service.RecuperacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
@RequestMapping("/recuperar")
public class RecuperacionController {

    private final RecuperacionService recuperacionService;

    public RecuperacionController(RecuperacionService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    // Formulario — ingresar email
    @GetMapping
    public String mostrarFormulario() {
        return "auth/recuperar";
    }

    @PostMapping
    public String procesarSolicitud(@RequestParam String email,
                                    RedirectAttributes redirect) {
        boolean enviado = recuperacionService.solicitarRecuperacion(email);
        if (enviado) {
            redirect.addFlashAttribute("exito",
                    "Te enviamos un enlace de recuperación a tu correo. Revisa tu bandeja de entrada.");
        } else {
            redirect.addFlashAttribute("error",
                    "No encontramos una cuenta con ese correo electrónico.");
        }
        return "redirect:/recuperar";
    }

    // Formulario — nueva contraseña
    @GetMapping("/reset")
    public String mostrarReset(@RequestParam String token, Model model) {
        Optional<TokenRecuperacion> tokenOpt = recuperacionService.validarToken(token);
        if (tokenOpt.isEmpty()) {
            model.addAttribute("error", "El enlace no es válido o ya expiró. Solicita uno nuevo.");
            return "auth/recuperar";
        }
        model.addAttribute("token", token);
        return "auth/reset";
    }

    @PostMapping("/reset")
    public String procesarReset(@RequestParam String token,
                                @RequestParam String password,
                                @RequestParam String confirmar,
                                RedirectAttributes redirect) {
        if (!password.equals(confirmar)) {
            redirect.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/recuperar/reset?token=" + token;
        }
        if (password.length() < 6) {
            redirect.addFlashAttribute("error", "La contraseña debe tener mínimo 6 caracteres.");
            return "redirect:/recuperar/reset?token=" + token;
        }
        boolean ok = recuperacionService.resetearContrasena(token, password);
        if (ok) {
            redirect.addFlashAttribute("exito", "Contraseña actualizada correctamente. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } else {
            redirect.addFlashAttribute("error", "El enlace expiró. Solicita uno nuevo.");
            return "redirect:/recuperar";
        }
    }
}