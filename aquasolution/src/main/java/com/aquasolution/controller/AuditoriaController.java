package com.aquasolution.controller;

import com.aquasolution.model.Auditoria;
import com.aquasolution.service.AuditoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String rol,
            Model model) {

        List<Auditoria> registros;

        if (usuario != null && !usuario.isBlank()) {
            registros = auditoriaService.filtrarPorUsuario(usuario);
            model.addAttribute("filtroUsuario", usuario);
        } else if (modulo != null && !modulo.isBlank()) {
            registros = auditoriaService.filtrarPorModulo(modulo);
            model.addAttribute("filtroModulo", modulo);
        } else if (rol != null && !rol.isBlank()) {
            registros = auditoriaService.filtrarPorRol(rol);
            model.addAttribute("filtroRol", rol);
        } else {
            registros = auditoriaService.obtenerTodos();
        }

        model.addAttribute("registros", registros);
        return "auditoria";
    }
}