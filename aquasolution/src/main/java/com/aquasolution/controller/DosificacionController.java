package com.aquasolution.controller;

import com.aquasolution.model.Dosificacion;
import com.aquasolution.model.Piscina;
import com.aquasolution.model.Usuario;
import com.aquasolution.service.DosificacionService;
import com.aquasolution.service.PiscinaService;
import com.aquasolution.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/dosificaciones")
public class DosificacionController {

    private final DosificacionService dosificacionService;
    private final PiscinaService piscinaService;
    private final UsuarioService usuarioService;

    public DosificacionController(DosificacionService dosificacionService,
                                  PiscinaService piscinaService,
                                  UsuarioService usuarioService) {
        this.dosificacionService = dosificacionService;
        this.piscinaService = piscinaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/guardar")
    public String guardarDosificacion(
            @RequestParam Long piscinaId,
            @RequestParam Long clienteId,
            @RequestParam String desinfectante,
            @RequestParam BigDecimal cloroLibre,
            @RequestParam BigDecimal ph,
            @RequestParam(required = false) BigDecimal alcalinidad,
            @RequestParam(required = false) BigDecimal calcio,
            @RequestParam(required = false) BigDecimal cya,
            @RequestParam(required = false) BigDecimal temperatura,
            @RequestParam(required = false) String aspecto,
            @RequestParam BigDecimal volumenGalones,
            @RequestParam(required = false) BigDecimal dosisDesinfectante,
            @RequestParam(required = false) BigDecimal dosisPhIncreaser,
            @RequestParam(required = false) BigDecimal dosisPhDecreaser,
            @RequestParam(required = false) BigDecimal dosisAlgicida,
            @RequestParam(required = false) BigDecimal dosisClarificador,
            @RequestParam(required = false) BigDecimal lsi,
            @RequestParam(required = false) String estadoLsi,
            @RequestParam(required = false) String observaciones,
            Authentication authentication,
            RedirectAttributes redirect) {

        Piscina piscina = piscinaService.obtenerPorId(piscinaId)
                .orElseThrow(() -> new RuntimeException("Piscina no encontrada"));
        Usuario cliente = usuarioService.obtenerPorId(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Técnico = usuario autenticado
        Usuario tecnico = usuarioService.obtenerPorUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Dosificacion dosificacion = new Dosificacion();
        dosificacion.setPiscina(piscina);
        dosificacion.setCliente(cliente);
        dosificacion.setTecnico(tecnico);
        dosificacion.setDesinfectante(desinfectante);
        dosificacion.setCloroLibre(cloroLibre);
        dosificacion.setPh(ph);
        dosificacion.setAlcalinidad(alcalinidad);
        dosificacion.setCalcio(calcio);
        dosificacion.setCya(cya);
        dosificacion.setTemperatura(temperatura != null ? temperatura : BigDecimal.valueOf(80));
        dosificacion.setAspecto(aspecto != null ? aspecto : "CRISTALINA");
        dosificacion.setVolumenGalones(volumenGalones);
        dosificacion.setDosisDesinfectante(dosisDesinfectante);
        dosificacion.setDosisPhIncreaser(dosisPhIncreaser);
        dosificacion.setDosisPhDecreaser(dosisPhDecreaser);
        dosificacion.setDosisAlgicida(dosisAlgicida);
        dosificacion.setDosisClarificador(dosisClarificador);
        dosificacion.setLsi(lsi);
        dosificacion.setEstadoLsi(estadoLsi);
        dosificacion.setObservaciones(observaciones);

        Dosificacion guardada = dosificacionService.guardar(dosificacion);
        redirect.addFlashAttribute("exito", "Dosificacion guardada correctamente.");
        return "redirect:/piscinas/detalle/" + piscinaId +
                "?desinfectante=" + desinfectante +
                "&cloroLibre=" + cloroLibre +
                "&ph=" + ph +
                (alcalinidad != null ? "&alcalinidad=" + alcalinidad : "") +
                (calcio != null ? "&calcio=" + calcio : "") +
                (cya != null ? "&cya=" + cya : "") +
                "&temperatura=" + dosificacion.getTemperatura() +
                "&aspecto=" + dosificacion.getAspecto() +
                "&dosificacionGuardada=" + guardada.getId();
    }

    @GetMapping("/pdf/{id}")
    public void descargarPDF(@PathVariable Long id,
                             HttpServletResponse response) throws Exception {
        byte[] pdf = dosificacionService.generarPDF(id);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=dosificacion-" + id + ".pdf");
        response.getOutputStream().write(pdf);
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           @RequestParam Long piscinaId,
                           RedirectAttributes redirect) {
        dosificacionService.eliminar(id);
        redirect.addFlashAttribute("exito", "Dosificacion eliminada.");
        return "redirect:/piscinas/detalle/" + piscinaId;
    }
}