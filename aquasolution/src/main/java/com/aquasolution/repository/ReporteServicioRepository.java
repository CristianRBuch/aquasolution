package com.aquasolution.repository;

import com.aquasolution.model.ReporteServicio;
import com.aquasolution.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReporteServicioRepository extends JpaRepository<ReporteServicio, Long> {
    List<ReporteServicio> findByTecnico(Usuario tecnico);
    List<ReporteServicio> findByCliente(Usuario cliente);
    List<ReporteServicio> findByTicketId(Long ticketId);
}