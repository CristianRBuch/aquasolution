package com.aquasolution.controller;

import com.aquasolution.model.Piscina;
import com.aquasolution.model.Usuario;
import com.aquasolution.service.PiscinaService;
import com.aquasolution.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/piscinas")
public class PiscinaController {

    private final PiscinaService piscinaService;
    private final UsuarioService usuarioService;

    public PiscinaController(PiscinaService piscinaService,
                             UsuarioService usuarioService) {
        this.piscinaService = piscinaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listarPiscinas(Model model, Authentication authentication) {
        model.addAttribute("piscinas", piscinaService.obtenerTodas());
        model.addAttribute("clientes", usuarioService.obtenerPorRol(Usuario.Rol.CLIENTE));
        model.addAttribute("piscinaNueva", new Piscina());
        return "piscinas/lista";
    }

    @PostMapping("/guardar")
    public String guardarPiscina(@ModelAttribute Piscina piscina,
                                 @RequestParam Long clienteId,
                                 RedirectAttributes redirect) {
        Usuario cliente = usuarioService.obtenerPorId(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        piscina.setCliente(cliente);
        piscinaService.guardar(piscina);
        redirect.addFlashAttribute("exito", "Piscina registrada correctamente.");
        return "redirect:/piscinas";
    }

    @GetMapping("/editar/{id}")
    public String editarPiscina(@PathVariable Long id, Model model) {
        Piscina piscina = piscinaService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Piscina no encontrada"));
        model.addAttribute("piscina", piscina);
        model.addAttribute("clientes", usuarioService.obtenerPorRol(Usuario.Rol.CLIENTE));
        return "piscinas/editar";
    }

    @PostMapping("/actualizar")
    public String actualizarPiscina(@ModelAttribute Piscina piscina,
                                    @RequestParam Long clienteId,
                                    RedirectAttributes redirect) {
        Usuario cliente = usuarioService.obtenerPorId(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        piscina.setCliente(cliente);
        piscinaService.guardar(piscina);
        redirect.addFlashAttribute("exito", "Piscina actualizada correctamente.");
        return "redirect:/piscinas";
    }

    @GetMapping("/detalle/{id}")
    public String detallePiscina(@PathVariable Long id, Model model) {
        Piscina piscina = piscinaService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Piscina no encontrada"));
        model.addAttribute("piscina", piscina);
        model.addAttribute("cloro", piscinaService.calcularCloro(piscina.getVolumen()));
        model.addAttribute("algicida", piscinaService.calcularAlgicida(piscina.getVolumen()));
        model.addAttribute("clarificante", piscinaService.calcularClarificante(piscina.getVolumen()));
        model.addAttribute("flujoBomba", piscinaService.calcularFlujoBomba(piscina.getVolumen()));
        return "piscinas/detalle";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPiscina(@PathVariable Long id, RedirectAttributes redirect) {
        piscinaService.eliminar(id);
        redirect.addFlashAttribute("exito", "Piscina eliminada correctamente.");
        return "redirect:/piscinas";
    }
}