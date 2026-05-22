package com.aquasolution.repository;

import com.aquasolution.model.Cotizacion;
import com.aquasolution.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    List<Cotizacion> findByCliente(Usuario cliente);
    List<Cotizacion> findByTecnico(Usuario tecnico);
    Optional<Cotizacion> findByNumeroCotizacion(String numeroCotizacion);
    List<Cotizacion> findByEstado(Cotizacion.EstadoCotizacion estado);
    boolean existsByNumeroCotizacion(String numeroCotizacion);
}