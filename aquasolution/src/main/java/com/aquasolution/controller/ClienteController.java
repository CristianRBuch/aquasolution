package com.aquasolution.controller;

import com.aquasolution.model.Ticket;
import com.aquasolution.model.Usuario;
import com.aquasolution.service.CotizacionService;
import com.aquasolution.service.DosificacionService;
import com.aquasolution.service.ReporteServicioService;
import com.aquasolution.service.TicketService;
import com.aquasolution.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClienteController {

    private final TicketService ticketService;
    private final CotizacionService cotizacionService;
    private final UsuarioService usuarioService;
    private final ReporteServicioService reporteService;
    private final DosificacionService dosificacionService;

    public ClienteController(TicketService ticketService,
                             CotizacionService cotizacionService,
                             UsuarioService usuarioService,
                             ReporteServicioService reporteService,
                             DosificacionService dosificacionService) {
        this.ticketService = ticketService;
        this.cotizacionService = cotizacionService;
        this.usuarioService = usuarioService;
        this.reporteService = reporteService;
        this.dosificacionService = dosificacionService;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute Usuario usuario,
                            RedirectAttributes redirect) {
        if (usuarioService.existeUsername(usuario.getUsername())) {
            redirect.addFlashAttribute("error", "El nombre de usuario ya existe.");
            return "redirect:/registro";
        }
        if (usuarioService.existeEmail(usuario.getEmail())) {
            redirect.addFlashAttribute("error", "El correo ya está registrado.");
            return "redirect:/registro";
        }
        usuario.setRol(Usuario.Rol.CLIENTE);
        usuario.setActivo(true);
        usuarioService.guardar(usuario);
        redirect.addFlashAttribute("exito", "Registro exitoso. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    @GetMapping("/cliente/inicio")
    public String inicio(Authentication authentication, Model model) {
        String username = authentication.getName();
        Usuario cliente = usuarioService.obtenerPorUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var tickets = ticketService.obtenerPorCliente(cliente);
        var cotizaciones = cotizacionService.obtenerPorCliente(cliente);
        var reportes = reporteService.obtenerPorCliente(cliente);
        var dosificaciones = dosificacionService.obtenerPorCliente(cliente);

        model.addAttribute("cliente", cliente);
        model.addAttribute("tickets", tickets);
        model.addAttribute("cotizaciones", cotizaciones);
        model.addAttribute("reportes", reportes);
        model.addAttribute("dosificaciones", dosificaciones);
        model.addAttribute("totalTickets", tickets.size());
        model.addAttribute("totalCotizaciones", cotizaciones.size());
        model.addAttribute("totalReportes", reportes.size());
        model.addAttribute("totalDosificaciones", dosificaciones.size());
        return "cliente/inicio";
    }

    @PostMapping("/cliente/solicitar")
    public String solicitarServicio(
            @RequestParam String titulo,
            @RequestParam String descripcion,
            @RequestParam String prioridad,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) String zona,
            @RequestParam(required = false) String direccionExacta,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String marcaEquipo,
            Authentication authentication,
            RedirectAttributes redirect) {

        String username = authentication.getName();
        Usuario cliente = usuarioService.obtenerPorUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        StringBuilder direccion = new StringBuilder();
        if (departamento != null && !departamento.isEmpty()) direccion.append(departamento);
        if (municipio != null && !municipio.isEmpty()) direccion.append(", ").append(municipio);
        if (zona != null && !zona.isEmpty()) direccion.append(", ").append(zona);
        if (direccionExacta != null && !direccionExacta.isEmpty()) direccion.append(" — ").append(direccionExacta);

        StringBuilder obs = new StringBuilder();
        if (observaciones != null && !observaciones.isEmpty()) obs.append("Disponibilidad: ").append(observaciones).append("\n");
        if (telefono != null && !telefono.isEmpty()) obs.append("Teléfono: ").append(telefono).append("\n");
        if (marcaEquipo != null && !marcaEquipo.isEmpty()) obs.append("Equipo: ").append(marcaEquipo);

        Ticket ticket = new Ticket();
        ticket.setTitulo(titulo);
        ticket.setDescripcion(descripcion);
        ticket.setPrioridad(Ticket.PrioridadTicket.valueOf(prioridad));
        ticket.setObservaciones(obs.toString());
        ticket.setDireccion(direccion.toString());
        ticket.setCliente(cliente);
        ticket.setEstado(Ticket.EstadoTicket.ABIERTO);
        ticketService.guardar(ticket);

        redirect.addFlashAttribute("exito", "Solicitud enviada correctamente. Pronto nos pondremos en contacto.");
        return "redirect:/cliente/inicio";
    }
}