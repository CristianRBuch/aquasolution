package com.aquasolution.controller;

import com.aquasolution.model.MaterialUtilizado;
import com.aquasolution.model.ReporteServicio;
import com.aquasolution.model.Ticket;
import com.aquasolution.model.Usuario;
import com.aquasolution.service.PiscinaService;
import com.aquasolution.service.ReporteServicioService;
import com.aquasolution.service.TicketService;
import com.aquasolution.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReporteServicioController {

    private final ReporteServicioService reporteService;
    private final TicketService ticketService;
    private final UsuarioService usuarioService;
    private final PiscinaService piscinaService;

    public ReporteServicioController(ReporteServicioService reporteService,
                                     TicketService ticketService,
                                     UsuarioService usuarioService,
                                     PiscinaService piscinaService) {
        this.reporteService = reporteService;
        this.ticketService = ticketService;
        this.usuarioService = usuarioService;
        this.piscinaService = piscinaService;
    }

    @GetMapping
    public String listarReportes(Model model, Authentication authentication) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        String username = authentication.getName();

        if (rol.equals("TECNICO")) {
            Usuario tecnico = usuarioService.obtenerPorUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            model.addAttribute("reportes", reporteService.obtenerPorTecnico(tecnico));
            model.addAttribute("tickets", ticketService.obtenerPorTecnico(tecnico));
        } else {
            model.addAttribute("reportes", reporteService.obtenerTodos());
            model.addAttribute("tickets", ticketService.obtenerTodos());
        }

        model.addAttribute("tecnicos", usuarioService.obtenerPorRol(Usuario.Rol.TECNICO));
        model.addAttribute("clientes", usuarioService.obtenerPorRol(Usuario.Rol.CLIENTE));
        model.addAttribute("piscinas", piscinaService.obtenerTodas());
        return "reportes/lista";
    }

    @PostMapping("/guardar")
    public String guardarReporte(
            @RequestParam Long ticketId,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam Long clienteId,
            @RequestParam(required = false) Long piscinaId,
            @RequestParam String fechaServicio,
            @RequestParam String tipoServicio,
            @RequestParam(required = false) String bombaPiscina,
            @RequestParam(required = false) String filtroTipo,
            @RequestParam(required = false) String filtroTamanio,
            @RequestParam(required = false) String lamparas,
            @RequestParam(required = false) String calentador,
            @RequestParam(required = false) String cantidadValvulas,
            @RequestParam(required = false) String diametroValvulas,
            @RequestParam(required = false) String bombaHidro,
            @RequestParam(required = false) String tanqueHidroneumatico,
            @RequestParam(required = false) String voltajeHidro,
            @RequestParam(required = false) String amperajeHidro,
            @RequestParam(required = false) String diametroPozo,
            @RequestParam(required = false) String diametroTuberia,
            @RequestParam(required = false) String cantidadTubos,
            @RequestParam(required = false) String potenciaBomba,
            @RequestParam(required = false) String potenciaMotor,
            @RequestParam(required = false) String tipoFase,
            @RequestParam(required = false) String voltajePozo,
            @RequestParam(required = false) String calibreCable,
            @RequestParam(required = false) String trabajoRealizado,
            @RequestParam(required = false) String proximaVisita,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) List<Integer> cantidad,
            @RequestParam(required = false) List<String> descripcionMaterial,
            @RequestParam(required = false) List<String> notas,
            Authentication authentication,
            RedirectAttributes redirect) {

        Ticket ticket = ticketService.obtenerPorId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        Usuario cliente = usuarioService.obtenerPorId(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Técnico — sin duplicado
        Usuario tecnico;
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        if (rol.equals("TECNICO")) {
            tecnico = usuarioService.obtenerPorUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        } else if (tecnicoId != null) {
            tecnico = usuarioService.obtenerPorId(tecnicoId)
                    .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
        } else if (ticket.getTecnico() != null) {
            tecnico = ticket.getTecnico();
        } else {
            redirect.addFlashAttribute("error", "El ticket no tiene técnico asignado. Asigna un técnico primero.");
            return "redirect:/reportes";
        }

        ReporteServicio reporte = new ReporteServicio();
        reporte.setTicket(ticket);
        reporte.setTecnico(tecnico);
        reporte.setCliente(cliente);
        reporte.setFechaServicio(LocalDateTime.parse(fechaServicio + "T00:00:00"));
        reporte.setTipoServicio(ReporteServicio.TipoServicio.valueOf(tipoServicio));

        if (piscinaId != null) {
            reporte.setPiscina(piscinaService.obtenerPorId(piscinaId).orElse(null));
        }

        reporte.setBombaPiscina(bombaPiscina);
        reporte.setFiltroTipo(filtroTipo);
        reporte.setFiltroTamanio(filtroTamanio);
        reporte.setLamparas(lamparas);
        reporte.setCalentador(calentador);
        reporte.setCantidadValvulas(cantidadValvulas);
        reporte.setDiametroValvulas(diametroValvulas);
        reporte.setBombaHidro(bombaHidro);
        reporte.setTanqueHidroneumatico(tanqueHidroneumatico);
        reporte.setVoltajeHidro(voltajeHidro);
        reporte.setAmperajeHidro(amperajeHidro);
        reporte.setDiametroPozo(diametroPozo);
        reporte.setDiametroTuberia(diametroTuberia);
        reporte.setCantidadTubos(cantidadTubos);
        reporte.setPotenciaBomba(potenciaBomba);
        reporte.setPotenciaMotor(potenciaMotor);
        reporte.setTipoFase(tipoFase);
        reporte.setVoltajePozo(voltajePozo);
        reporte.setCalibreCable(calibreCable);
        reporte.setTrabajoRealizado(trabajoRealizado);
        reporte.setProximaVisita(proximaVisita);
        reporte.setObservaciones(observaciones);

        List<MaterialUtilizado> materiales = new ArrayList<>();
        if (cantidad != null) {
            for (int i = 0; i < cantidad.size(); i++) {
                if (descripcionMaterial != null && i < descripcionMaterial.size()
                        && !descripcionMaterial.get(i).isEmpty()) {
                    MaterialUtilizado m = new MaterialUtilizado();
                    m.setCantidad(cantidad.get(i));
                    m.setDescripcion(descripcionMaterial.get(i));
                    m.setNotas(notas != null && i < notas.size() ? notas.get(i) : "");
                    materiales.add(m);
                }
            }
        }
        reporte.setMateriales(materiales);
        reporteService.guardar(reporte);
        ticketService.cambiarEstado(ticketId, Ticket.EstadoTicket.RESUELTO);

        redirect.addFlashAttribute("exito", "Reporte guardado correctamente.");
        return "redirect:/reportes";
    }

    @GetMapping("/pdf/{id}")
    public void descargarPDF(@PathVariable Long id, HttpServletResponse response) throws Exception {
        byte[] pdf = reporteService.generarPDF(id);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte-" + id + ".pdf");
        response.getOutputStream().write(pdf);
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           Authentication authentication,
                           RedirectAttributes redirect) {
        String rol = authentication.getAuthorities().iterator().next().getAuthority();
        if (!rol.equals("ADMIN")) {
            redirect.addFlashAttribute("error", "No tienes permisos para eliminar reportes.");
            return "redirect:/reportes";
        }
        reporteService.eliminar(id);
        redirect.addFlashAttribute("exito", "Reporte eliminado correctamente.");
        return "redirect:/reportes";
    }
}