package com.aquasolution.repository;

import com.aquasolution.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByOrderByFechaHoraDesc();

    List<Auditoria> findByUsuarioContainingIgnoreCaseOrderByFechaHoraDesc(String usuario);

    List<Auditoria> findByModuloOrderByFechaHoraDesc(String modulo);

    List<Auditoria> findByRolOrderByFechaHoraDesc(String rol);
}