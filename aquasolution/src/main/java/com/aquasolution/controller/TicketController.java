package com.aquasolution.controller;

import com.aquasolution.model.Piscina;
import com.aquasolution.model.Ticket;
import com.aquasolution.model.Usuario;
import com.aquasolution.service.PiscinaService;
import com.aquasolution.service.TicketService;
import com.aquasolution.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UsuarioService usuarioService;
    private final PiscinaService piscinaService;

    public TicketController(TicketService ticketService,
                            UsuarioService usuarioService,
                            PiscinaService piscinaService) {
        this.ticketService = ticketService;
        this.usuarioService = usuarioService;
        this.piscinaService = piscinaService;
    }

    @GetMapping
    public String listarTickets(Model model, Authentication authentication) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        String username = authentication.getName();

        if (rol.equals("TECNICO")) {
            Usuario tecnico = usuarioService.obtenerPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            model.addAttribute("tickets", ticketService.obtenerPorTecnico(tecnico));
        } else {
            model.addAttribute("tickets", ticketService.obtenerTodos());
        }

        model.addAttribute("clientes", usuarioService.obtenerPorRol(Usuario.Rol.CLIENTE));
        model.addAttribute("tecnicos", usuarioService.obtenerPorRol(Usuario.Rol.TECNICO));
        model.addAttribute("piscinas", piscinaService.obtenerTodas());
        model.addAttribute("rol", rol);
        return "tickets/lista";
    }

    @PostMapping("/guardar")
    public String guardarTicket(@ModelAttribute Ticket ticket,
                                @RequestParam Long clienteId,
                                @RequestParam(required = false) Long piscinaId,
                                @RequestParam(required = false) Long tecnicoId,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        Usuario cliente = usuarioService.obtenerPorId(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        ticket.setCliente(cliente);

        if (piscinaId != null) {
            Piscina piscina = piscinaService.obtenerPorId(piscinaId).orElse(null);
            ticket.setPiscina(piscina);
        }

        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        String usuarioActual = authentication.getName();

        if (rol.equals("TECNICO")) {
            Usuario tecnico = usuarioService.obtenerPorUsername(usuarioActual)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            ticket.setTecnico(tecnico);
        } else if (tecnicoId != null) {
            Usuario tecnico = usuarioService.obtenerPorId(tecnicoId).orElse(null);
            ticket.setTecnico(tecnico);
        }

        ticket.setEstado(Ticket.EstadoTicket.ABIERTO);
        ticketService.guardar(ticket, usuarioActual, rol);
        redirect.addFlashAttribute("exito", "Ticket creado correctamente.");
        return "redirect:/tickets";
    }

    @PostMapping("/asignar/{id}")
    public String asignarTecnico(@PathVariable Long id,
                                 @RequestParam Long tecnicoId,
                                 @RequestParam(required = false) String fechaVisita,
                                 Authentication authentication,
                                 RedirectAttributes redirect) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        String usuarioActual = authentication.getName();

        if (!rol.equals("ADMIN")) {
            redirect.addFlashAttribute("error", "No tienes permisos para reasignar tickets.");
            return "redirect:/tickets";
        }
        Usuario tecnico = usuarioService.obtenerPorId(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
        Ticket ticket = ticketService.asignarTecnico(id, tecnico, usuarioActual, rol);

        if (fechaVisita != null && !fechaVisita.isEmpty()) {
            ticket.setFechaVisita(java.time.LocalDateTime.parse(fechaVisita + "T00:00:00"));
            ticketService.guardar(ticket, usuarioActual, rol);
        }

        redirect.addFlashAttribute("exito", "Técnico asignado correctamente.");
        return "redirect:/tickets";
    }

    @GetMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        String usuarioActual = authentication.getName();
        ticketService.cambiarEstado(id, Ticket.EstadoTicket.valueOf(estado), usuarioActual, rol);
        redirect.addFlashAttribute("exito", "Estado actualizado correctamente.");
        return "redirect:/tickets";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarTicket(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirect) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        String usuarioActual = authentication.getName();

        if (!rol.equals("ADMIN")) {
            redirect.addFlashAttribute("error", "No tienes permisos para eliminar tickets.");
            return "redirect:/tickets";
        }
        ticketService.eliminar(id, usuarioActual, rol);
        redirect.addFlashAttribute("exito", "Ticket eliminado correctamente.");
        return "redirect:/tickets";
    }
}