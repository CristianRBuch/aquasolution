package com.aquasolution.service;

import com.aquasolution.model.Ticket;
import com.aquasolution.model.Usuario;
import com.aquasolution.repository.TicketRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket guardar(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public List<Ticket> obtenerTodos() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> obtenerPorId(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> obtenerPorCliente(Usuario cliente) {
        return ticketRepository.findByCliente(cliente);
    }

    public List<Ticket> obtenerPorTecnico(Usuario tecnico) {
        return ticketRepository.findByTecnico(tecnico);
    }

    public List<Ticket> obtenerPorEstado(Ticket.EstadoTicket estado) {
        return ticketRepository.findByEstado(estado);
    }

    public List<Ticket> obtenerPorTecnicoYEstado(Usuario tecnico, Ticket.EstadoTicket estado) {
        return ticketRepository.findByTecnicoAndEstado(tecnico, estado);
    }

    public List<Usuario> obtenerClientesPorTecnico(Usuario tecnico) {
        // Obtiene clientes únicos de todos los tickets asignados al técnico
        return ticketRepository.findByTecnico(tecnico)
                .stream()
                .map(Ticket::getCliente)
                .distinct()
                .collect(Collectors.toList());
    }

    public Ticket cambiarEstado(Long id, Ticket.EstadoTicket nuevoEstado) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        ticket.setEstado(nuevoEstado);
        if (nuevoEstado == Ticket.EstadoTicket.CERRADO) {
            ticket.setFechaCierre(LocalDateTime.now());
        }
        return ticketRepository.save(ticket);
    }

    public Ticket asignarTecnico(Long ticketId, Usuario tecnico) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        ticket.setTecnico(tecnico);
        ticket.setEstado(Ticket.EstadoTicket.EN_PROCESO);
        return ticketRepository.save(ticket);
    }

    public void eliminar(Long id) {
        ticketRepository.deleteById(id);
    }

    public List<Ticket> obtenerUltimos5() {
        return ticketRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getFechaCreacion().compareTo(a.getFechaCreacion()))
                .limit(5)
                .collect(Collectors.toList());
    }
}