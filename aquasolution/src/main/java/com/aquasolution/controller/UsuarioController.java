package com.aquasolution.controller;

import com.aquasolution.model.Ticket;
import com.aquasolution.model.Usuario;
import com.aquasolution.service.CotizacionService;
import com.aquasolution.service.PiscinaService;
import com.aquasolution.service.ProductoService;
import com.aquasolution.service.ReporteServicioService;
import com.aquasolution.service.TicketService;
import com.aquasolution.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TicketService ticketService;
    private final PiscinaService piscinaService;
    private final CotizacionService cotizacionService;
    private final ReporteServicioService reporteServicioService;
    private final ProductoService productoService;

    public UsuarioController(UsuarioService usuarioService,
                             TicketService ticketService,
                             PiscinaService piscinaService,
                             CotizacionService cotizacionService,
                             ReporteServicioService reporteServicioService,
                             ProductoService productoService) {
        this.usuarioService = usuarioService;
        this.ticketService = ticketService;
        this.piscinaService = piscinaService;
        this.cotizacionService = cotizacionService;
        this.reporteServicioService = reporteServicioService;
        this.productoService = productoService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        model.addAttribute("username", username);
        model.addAttribute("rol", rol);

        if (rol.equals("TECNICO")) {
            Usuario tecnico = usuarioService.obtenerPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            model.addAttribute("misTickets",
                    ticketService.obtenerPorTecnico(tecnico).size());
            model.addAttribute("ticketsAbiertos",
                    ticketService.obtenerPorTecnicoYEstado(tecnico, Ticket.EstadoTicket.ABIERTO).size());
            model.addAttribute("ticketsEnProceso",
                    ticketService.obtenerPorTecnicoYEstado(tecnico, Ticket.EstadoTicket.EN_PROCESO).size());
            model.addAttribute("totalReportes",
                    reporteServicioService.obtenerPorTecnico(tecnico).size());
            return "tecnico/dashboard";
        }

        // ADMIN
        model.addAttribute("ticketsAbiertos",
                ticketService.obtenerPorEstado(Ticket.EstadoTicket.ABIERTO).size());
        model.addAttribute("ticketsEnProceso",
                ticketService.obtenerPorEstado(Ticket.EstadoTicket.EN_PROCESO).size());
        model.addAttribute("ticketsResueltos",
                ticketService.obtenerPorEstado(Ticket.EstadoTicket.RESUELTO).size());
        model.addAttribute("totalPiscinas",
                piscinaService.obtenerTodas().size());
        model.addAttribute("totalCotizaciones",
                cotizacionService.obtenerTodas().size());
        model.addAttribute("totalClientes",
                usuarioService.obtenerPorRol(Usuario.Rol.CLIENTE).size());
        model.addAttribute("totalTecnicos",
                usuarioService.obtenerPorRol(Usuario.Rol.TECNICO).size());
        model.addAttribute("cotizacionesPendientes",
                cotizacionService.obtenerTodas().stream()
                        .filter(c -> c.getEstado().name().equals("PENDIENTE")).count());
        model.addAttribute("ticketsUrgentes",
                ticketService.obtenerPorEstado(Ticket.EstadoTicket.ABIERTO).stream()
                        .filter(t -> t.getPrioridad().name().equals("URGENTE")).count());
        model.addAttribute("ultimosTickets", ticketService.obtenerUltimos5());
        model.addAttribute("alertasStock", productoService.obtenerConStockBajo());

        return "dashboard";
    }
}