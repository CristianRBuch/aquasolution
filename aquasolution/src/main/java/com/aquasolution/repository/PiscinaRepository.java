package com.aquasolution.repository;

import com.aquasolution.model.Piscina;
import com.aquasolution.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PiscinaRepository extends JpaRepository<Piscina, Long> {

    List<Piscina> findByCliente(Usuario cliente);
    List<Piscina> findByTipo(Piscina.TipoPiscina tipo);
}