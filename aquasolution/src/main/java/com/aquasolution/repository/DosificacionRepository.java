package com.aquasolution.repository;

import com.aquasolution.model.Dosificacion;
import com.aquasolution.model.Piscina;
import com.aquasolution.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DosificacionRepository extends JpaRepository<Dosificacion, Long> {
    List<Dosificacion> findByPiscina(Piscina piscina);
    List<Dosificacion> findByCliente(Usuario cliente);
    List<Dosificacion> findByTecnico(Usuario tecnico);
    boolean existsByNumeroDosificacion(String numeroDosificacion);
}