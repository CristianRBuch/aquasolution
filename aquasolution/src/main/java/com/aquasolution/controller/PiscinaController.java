package com.aquasolution.controller;

import com.aquasolution.model.Dosificacion;
import com.aquasolution.model.Piscina;
import com.aquasolution.model.Usuario;
import com.aquasolution.service.DosificacionService;
import com.aquasolution.service.PiscinaService;
import com.aquasolution.service.UsuarioService;
import com.aquasolution.util.CalculoHidraulicoUtil;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/piscinas")
public class PiscinaController {

    private final PiscinaService piscinaService;
    private final UsuarioService usuarioService;
    private final CalculoHidraulicoUtil calculoUtil;
    private final DosificacionService dosificacionService;

    public PiscinaController(PiscinaService piscinaService,
                             UsuarioService usuarioService,
                             CalculoHidraulicoUtil calculoUtil,
                             DosificacionService dosificacionService) {
        this.piscinaService = piscinaService;
        this.usuarioService = usuarioService;
        this.calculoUtil = calculoUtil;
        this.dosificacionService = dosificacionService;
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
    public String detallePiscina(@PathVariable Long id,
                                 @RequestParam(required = false) String desinfectante,
                                 @RequestParam(required = false) Double cloroLibre,
                                 @RequestParam(required = false) Double ph,
                                 @RequestParam(required = false) Double alcalinidad,
                                 @RequestParam(required = false) Double calcio,
                                 @RequestParam(required = false) Double cya,
                                 @RequestParam(required = false, defaultValue = "80") Double temperatura,
                                 @RequestParam(required = false, defaultValue = "CRISTALINA") String aspecto,
                                 @RequestParam(required = false) Long dosificacionGuardada,
                                 Model model) {

        Piscina piscina = piscinaService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Piscina no encontrada"));

        double volumenGalones = piscina.getVolumen() * 264.172;

        model.addAttribute("piscina", piscina);
        model.addAttribute("volumenGalones", calculoUtil.redondear(volumenGalones));
        model.addAttribute("cloro", piscinaService.calcularCloro(piscina.getVolumen()));
        model.addAttribute("algicida", piscinaService.calcularAlgicida(piscina.getVolumen()));
        model.addAttribute("clarificante", piscinaService.calcularClarificante(piscina.getVolumen()));
        model.addAttribute("flujoBomba", piscinaService.calcularFlujoBomba(piscina.getVolumen()));

        // Historial de dosificaciones
        List<Dosificacion> historialDosificaciones =
                dosificacionService.obtenerPorPiscina(piscina);
        model.addAttribute("historialDosificaciones", historialDosificaciones);

        // Dosificacion recien guardada para mostrar boton PDF
        if (dosificacionGuardada != null) {
            model.addAttribute("dosificacionGuardada", dosificacionGuardada);
        }

        if (ph != null && cloroLibre != null) {
            model.addAttribute("estadoCloro", calculoUtil.estadoCloro(cloroLibre));
            model.addAttribute("estadoPH", calculoUtil.estadoPH(ph));
            model.addAttribute("estadoAlcalinidad", alcalinidad != null ?
                    calculoUtil.estadoAlcalinidad(alcalinidad) : "GRIS");
            model.addAttribute("estadoCalcio", calcio != null ?
                    calculoUtil.estadoCalcio(calcio) : "GRIS");
            model.addAttribute("estadoCYA", cya != null ?
                    calculoUtil.estadoCYA(cya) : "GRIS");

            double ppmFaltante = Math.max(0, 3.0 - cloroLibre);
            double dosisDesinfectante = 0;
            if ("CHLOR65".equals(desinfectante)) {
                dosisDesinfectante = calculoUtil.calcularChlor65(volumenGalones, ppmFaltante);
            } else if ("TRICHLOR90".equals(desinfectante)) {
                dosisDesinfectante = calculoUtil.calcularTrichlor90(volumenGalones, ppmFaltante);
            }
            model.addAttribute("dosisDesinfectante", calculoUtil.redondear(dosisDesinfectante));

            double phObjetivo = 7.5;
            model.addAttribute("dosisPhIncreaser", calculoUtil.redondear(
                    calculoUtil.calcularPhIncreaser(volumenGalones, ph, phObjetivo)));
            model.addAttribute("dosisPhDecreaser", calculoUtil.redondear(
                    calculoUtil.calcularPhDecreaser(volumenGalones, ph, phObjetivo)));

            double dosisAlgicida = 0;
            switch (aspecto) {
                case "ALGAS_VERDES":
                    dosisAlgicida = calculoUtil.calcularAlgicidaDoble(volumenGalones);
                    break;
                case "ALGAS_NEGRAS":
                    dosisAlgicida = calculoUtil.calcularAlgicidaTriple(volumenGalones);
                    break;
                case "TURBIA":
                case "LIGERAMENTE_TURBIA":
                    dosisAlgicida = calculoUtil.calcularAlgicidaGalones(volumenGalones);
                    break;
                default:
                    dosisAlgicida = 0;
            }
            model.addAttribute("dosisAlgicida", calculoUtil.redondear(dosisAlgicida));

            double dosisClarificador = 0;
            if ("LIGERAMENTE_TURBIA".equals(aspecto)) {
                dosisClarificador = calculoUtil.calcularClarificadorSuperBlue(volumenGalones);
            } else if ("TURBIA".equals(aspecto)) {
                dosisClarificador = calculoUtil.calcularClarificadorClearAqua(volumenGalones) * 2;
            }
            model.addAttribute("dosisClarificador", calculoUtil.redondear(dosisClarificador));

            if (calcio != null && alcalinidad != null) {
                double lsi = calculoUtil.calcularLSI(ph, temperatura, calcio, alcalinidad);
                model.addAttribute("lsi", lsi);
                model.addAttribute("estadoLSI", calculoUtil.interpretarLSI(lsi));
            }

            model.addAttribute("mostrarReporte", true);
            model.addAttribute("desinfectante", desinfectante);
            model.addAttribute("cloroLibre", cloroLibre);
            model.addAttribute("ph", ph);
            model.addAttribute("alcalinidad", alcalinidad);
            model.addAttribute("calcio", calcio);
            model.addAttribute("cya", cya);
            model.addAttribute("temperatura", temperatura);
            model.addAttribute("aspecto", aspecto);
        }

        return "piscinas/detalle";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPiscina(@PathVariable Long id, RedirectAttributes redirect) {
        piscinaService.eliminar(id);
        redirect.addFlashAttribute("exito", "Piscina eliminada correctamente.");
        return "redirect:/piscinas";
    }
}