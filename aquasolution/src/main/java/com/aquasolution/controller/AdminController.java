package com.aquasolution.controller;

import com.aquasolution.model.Usuario;
import com.aquasolution.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodos());
        model.addAttribute("usuarioNuevo", new Usuario());
        return "admin/usuarios";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario,
                                 RedirectAttributes redirect) {
        if (usuarioService.existeUsername(usuario.getUsername())) {
            redirect.addFlashAttribute("error", "El usuario ya existe.");
            return "redirect:/admin/usuarios";
        }
        usuarioService.guardar(usuario);
        redirect.addFlashAttribute("exito", "Usuario creado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/editar")
    public String editarUsuario(@RequestParam Long id,
                                @RequestParam String nombreCompleto,
                                @RequestParam String email,
                                @RequestParam String telefono,
                                @RequestParam String rol,
                                @RequestParam(required = false) String activo,
                                RedirectAttributes redirect) {
        Usuario usuario = usuarioService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setRol(Usuario.Rol.valueOf(rol));
        usuario.setActivo(activo != null && activo.equals("true"));
        usuarioService.actualizarSinPassword(usuario);
        redirect.addFlashAttribute("exito", "Usuario actualizado correctamente.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/cambiarPassword")
    public String cambiarPassword(@RequestParam Long id,
                                  @RequestParam String nuevaPassword,
                                  @RequestParam String confirmarPassword,
                                  RedirectAttributes redirect) {
        if (!nuevaPassword.equals(confirmarPassword)) {
            redirect.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/admin/usuarios";
        }
        if (nuevaPassword.length() < 6) {
            redirect.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/admin/usuarios";
        }
        usuarioService.cambiarPassword(id, nuevaPassword);
        redirect.addFlashAttribute("exito", "Contraseña actualizada correctamente.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id,
                                  RedirectAttributes redirect) {
        usuarioService.eliminar(id);
        redirect.addFlashAttribute("exito", "Usuario eliminado correctamente.");
        return "redirect:/admin/usuarios";
    }
}