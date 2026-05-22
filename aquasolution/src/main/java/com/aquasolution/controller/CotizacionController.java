package com.aquasolution.controller;

import com.aquasolution.model.Cotizacion;
import com.aquasolution.model.DetalleCotizacion;
import com.aquasolution.model.Ticket;
import com.aquasolution.model.Usuario;
import com.aquasolution.service.CotizacionService;
import com.aquasolution.service.ProductoService;
import com.aquasolution.service.TicketService;
import com.aquasolution.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final UsuarioService usuarioService;
    private final TicketService ticketService;
    private final ProductoService productoService;

    public CotizacionController(CotizacionService cotizacionService,
                                UsuarioService usuarioService,
                                TicketService ticketService,
                                ProductoService productoService) {
        this.cotizacionService = cotizacionService;
        this.usuarioService = usuarioService;
        this.ticketService = ticketService;
        this.productoService = productoService;
    }

    @GetMapping
    public String listarCotizaciones(Model model) {
        model.addAttribute("cotizaciones", cotizacionService.obtenerTodas());
        model.addAttribute("clientes", usuarioService.obtenerPorRol(Usuario.Rol.CLIENTE));
        model.addAttribute("tecnicos", usuarioService.obtenerPorRol(Usuario.Rol.TECNICO));
        model.addAttribute("tickets", ticketService.obtenerTodos());
        model.addAttribute("productos", productoService.obtenerActivos());
        return "cotizaciones/lista";
    }

    @PostMapping("/guardar")
    public String guardarCotizacion(
            @RequestParam Long clienteId,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam Long ticketId,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) String validezOferta,
            @RequestParam(required = false) String tiempoEntrega,
            @RequestParam(required = false) String condicionesPago,
            @RequestParam(required = false) String garantia,
            @RequestParam(required = false) String notaPrecios,
            @RequestParam List<String> descripcion,
            @RequestParam List<String> unidad,
            @RequestParam List<Integer> cantidad,
            @RequestParam List<BigDecimal> precioUnitario,
            @RequestParam(required = false) List<BigDecimal> descuento,
            @RequestParam(required = false) List<BigDecimal> subtotalDetalle,
            RedirectAttributes redirect) {

        Usuario cliente = usuarioService.obtenerPorId(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Ticket ticket = ticketService.obtenerPorId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // Crear cotización PRIMERO
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setCliente(cliente);
        cotizacion.setTicket(ticket);
        cotizacion.setObservaciones(observaciones);
        cotizacion.setValidezOferta(validezOferta);
        cotizacion.setTiempoEntrega(tiempoEntrega);
        cotizacion.setCondicionesPago(condicionesPago);
        cotizacion.setGarantia(garantia);
        cotizacion.setNotaPrecios(notaPrecios);

        // Técnico opcional — DESPUÉS de crear cotizacion
        if (tecnicoId != null) {
            Usuario tecnico = usuarioService.obtenerPorId(tecnicoId)
                    .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
            cotizacion.setTecnico(tecnico);
        }

        List<DetalleCotizacion> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < descripcion.size(); i++) {
            if (!descripcion.get(i).isEmpty()) {
                DetalleCotizacion detalle = new DetalleCotizacion();
                detalle.setDescripcion(descripcion.get(i));
                detalle.setUnidad(unidad.get(i));
                detalle.setCantidad(cantidad.get(i));
                detalle.setPrecioUnitario(precioUnitario.get(i));

                BigDecimal desc = (descuento != null && i < descuento.size())
                        ? descuento.get(i) : BigDecimal.ZERO;
                if (desc == null) desc = BigDecimal.ZERO;
                detalle.setDescuento(desc);

                BigDecimal sub = precioUnitario.get(i)
                        .multiply(new BigDecimal(cantidad.get(i)))
                        .multiply(BigDecimal.ONE.subtract(desc.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)))
                        .setScale(2, RoundingMode.HALF_UP);
                detalle.setSubtotal(sub);
                total = total.add(sub);
                detalles.add(detalle);
            }
        }

        cotizacion.setDetalles(detalles);
        cotizacion.setSubtotal(total);
        cotizacion.setTotal(total);
        cotizacionService.guardar(cotizacion);
        redirect.addFlashAttribute("exito", "Cotización creada correctamente.");
        return "redirect:/cotizaciones";
    }

    @PostMapping("/editar")
    public String editarCotizacion(
            @RequestParam Long id,
            @RequestParam(required = false) String observaciones,
            @RequestParam(required = false) String validezOferta,
            @RequestParam(required = false) String tiempoEntrega,
            @RequestParam(required = false) String condicionesPago,
            @RequestParam(required = false) String garantia,
            @RequestParam(required = false) String notaPrecios,
            @RequestParam List<String> descripcion,
            @RequestParam List<String> unidad,
            @RequestParam List<Integer> cantidad,
            @RequestParam List<BigDecimal> precioUnitario,
            @RequestParam(required = false) List<BigDecimal> descuento,
            RedirectAttributes redirect) {

        Cotizacion cotizacion = cotizacionService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

        cotizacion.setObservaciones(observaciones);
        cotizacion.setValidezOferta(validezOferta);
        cotizacion.setTiempoEntrega(tiempoEntrega);
        cotizacion.setCondicionesPago(condicionesPago);
        cotizacion.setGarantia(garantia);
        cotizacion.setNotaPrecios(notaPrecios);

        // Limpiar y reasignar detalles
        List<DetalleCotizacion> detallesActuales = cotizacion.getDetalles();
        detallesActuales.clear();

        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < descripcion.size(); i++) {
            if (descripcion.get(i) != null && !descripcion.get(i).isEmpty()) {
                DetalleCotizacion detalle = new DetalleCotizacion();
                detalle.setCotizacion(cotizacion);
                detalle.setDescripcion(descripcion.get(i));
                detalle.setUnidad(unidad.get(i));
                detalle.setCantidad(cantidad.get(i));
                detalle.setPrecioUnitario(precioUnitario.get(i));

                BigDecimal desc = (descuento != null && i < descuento.size())
                        ? descuento.get(i) : BigDecimal.ZERO;
                if (desc == null) desc = BigDecimal.ZERO;
                detalle.setDescuento(desc);

                BigDecimal sub = precioUnitario.get(i)
                        .multiply(new BigDecimal(cantidad.get(i)))
                        .multiply(BigDecimal.ONE.subtract(desc.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)))
                        .setScale(2, RoundingMode.HALF_UP);
                detalle.setSubtotal(sub);
                total = total.add(sub);
                detallesActuales.add(detalle);
            }
        }

        cotizacion.setDetalles(detallesActuales);
        cotizacion.setSubtotal(total);
        cotizacion.setTotal(total);
        cotizacionService.guardar(cotizacion);
        redirect.addFlashAttribute("exito", "Cotización actualizada correctamente.");
        return "redirect:/cotizaciones";
    }

    @GetMapping("/pdf/{id}")
    public void descargarPDF(@PathVariable Long id, HttpServletResponse response) throws Exception {
        byte[] pdf = cotizacionService.generarPDF(id);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=cotizacion-" + id + ".pdf");
        response.getOutputStream().write(pdf);
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarCotizacion(@PathVariable Long id, RedirectAttributes redirect) {
        cotizacionService.eliminar(id);
        redirect.addFlashAttribute("exito", "Cotización eliminada correctamente.");
        return "redirect:/cotizaciones";
    }

    @GetMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado,
                                RedirectAttributes redirect) {
        Cotizacion.EstadoCotizacion nuevoEstado = Cotizacion.EstadoCotizacion.valueOf(estado);

        // Si se aprueba, descontar stock de productos
        if (nuevoEstado == Cotizacion.EstadoCotizacion.APROBADA) {
            Cotizacion cotizacion = cotizacionService.obtenerPorId(id)
                    .orElseThrow(() -> new RuntimeException("Cotización no encontrada"));

            if (cotizacion.getDetalles() != null) {
                for (DetalleCotizacion detalle : cotizacion.getDetalles()) {
                    if (detalle.getProducto() != null) {
                        try {
                            productoService.actualizarStock(
                                    detalle.getProducto().getId(),
                                    detalle.getCantidad()
                            );
                        } catch (Exception e) {
                            redirect.addFlashAttribute("error",
                                    "Stock insuficiente para: " + detalle.getDescripcion());
                            return "redirect:/cotizaciones";
                        }
                    }
                }
            }
        }

        cotizacionService.cambiarEstado(id, nuevoEstado);
        redirect.addFlashAttribute("exito", "Estado actualizado correctamente.");
        return "redirect:/cotizaciones";
    }
}