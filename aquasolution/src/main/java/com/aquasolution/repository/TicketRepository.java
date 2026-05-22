package com.aquasolution.repository;

import com.aquasolution.model.Ticket;
import com.aquasolution.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCliente(Usuario cliente);
    List<Ticket> findByTecnico(Usuario tecnico);
    List<Ticket> findByEstado(Ticket.EstadoTicket estado);
    List<Ticket> findByClienteAndEstado(Usuario cliente, Ticket.EstadoTicket estado);
    List<Ticket> findByTecnicoAndEstado(Usuario tecnico, Ticket.EstadoTicket estado);
    List<Usuario> findDistinctClienteByTecnico(Usuario tecnico);
}