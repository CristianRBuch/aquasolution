package com.aquasolution.service;

import com.aquasolution.model.Auditoria;
import com.aquasolution.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    public void registrar(String usuario, String rol, String accion, String modulo, String descripcion) {
        Auditoria auditoria = new Auditoria(usuario, rol, accion, modulo, descripcion);
        auditoriaRepository.save(auditoria);
    }

    public List<Auditoria> obtenerTodos() {
        return auditoriaRepository.findAllByOrderByFechaHoraDesc();
    }

    public List<Auditoria> filtrarPorUsuario(String usuario) {
        return auditoriaRepository.findByUsuarioContainingIgnoreCaseOrderByFechaHoraDesc(usuario);
    }

    public List<Auditoria> filtrarPorModulo(String modulo) {
        return auditoriaRepository.findByModuloOrderByFechaHoraDesc(modulo);
    }

    public List<Auditoria> filtrarPorRol(String rol) {
        return auditoriaRepository.findByRolOrderByFechaHoraDesc(rol);
    }
}