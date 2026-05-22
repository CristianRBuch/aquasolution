package com.aquasolution.controller;

import com.aquasolution.model.Usuario;
import com.aquasolution.service.ReporteServicioService;
import com.aquasolution.service.TicketService;
import com.aquasolution.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/historial")
public class HistorialClienteController {

    private final UsuarioService usuarioService;
    private final ReporteServicioService reporteService;
    private final TicketService ticketService;

    public HistorialClienteController(UsuarioService usuarioService,
                                      ReporteServicioService reporteService,
                                      TicketService ticketService) {
        this.usuarioService = usuarioService;
        this.reporteService = reporteService;
        this.ticketService = ticketService;
    }

    @GetMapping
    public String listarClientes(Model model, Authentication authentication) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        String username = authentication.getName();

        if (rol.equals("TECNICO")) {
            Usuario tecnico = usuarioService.obtenerPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            // Solo clientes con tickets asignados a este técnico
            model.addAttribute("clientes",
                    ticketService.obtenerClientesPorTecnico(tecnico));
        } else {
            // Admin ve todos los clientes
            model.addAttribute("clientes",
                    usuarioService.obtenerPorRol(Usuario.Rol.CLIENTE));
        }
        return "historial/clientes";
    }

    @GetMapping("/{clienteId}")
    public String verHistorialCliente(@PathVariable Long clienteId,
                                      Model model,
                                      Authentication authentication) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        String username = authentication.getName();

        Usuario cliente = usuarioService.obtenerPorId(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Verificar que técnico tenga acceso a este cliente
        if (rol.equals("TECNICO")) {
            Usuario tecnico = usuarioService.obtenerPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            boolean tieneAcceso = ticketService.obtenerClientesPorTecnico(tecnico)
                    .stream().anyMatch(c -> c.getId().equals(clienteId));
            if (!tieneAcceso) {
                return "redirect:/historial";
            }
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("tickets", ticketService.obtenerPorCliente(cliente));
        model.addAttribute("reportes", reporteService.obtenerPorCliente(cliente));
        return "historial/detalle";
    }
}